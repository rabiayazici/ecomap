import os, io, base64, cv2, numpy as np, networkx as nx
from flask import Flask, request, jsonify
from flask_cors import CORS
from PIL import Image
import torch
from torchvision import transforms
from skimage.morphology import skeletonize
import segmentation_models_pytorch as smp
import tifffile as tiff
import gc

app = Flask(__name__)

# Enable CORS for all routes
CORS(app, 
     origins=["http://localhost:5173", "http://localhost:4444"],
     allow_headers=["Content-Type", "Authorization", "Accept", "Origin", "X-Requested-With"],
     supports_credentials=True,
     expose_headers=["Content-Type", "Authorization"])

@app.after_request
def after_request(response):
    response.headers.add('Access-Control-Allow-Origin', 'http://localhost:5173')
    response.headers.add('Access-Control-Allow-Headers', 'Content-Type,Authorization')
    response.headers.add('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS')
    response.headers.add('Access-Control-Allow-Credentials', 'true')
    return response

# ------------------------------------------------------------------
# Model load with memory management
# ------------------------------------------------------------------
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Clear any existing cached memory
torch.cuda.empty_cache() if torch.cuda.is_available() else None
gc.collect()

try:
    model = smp.Unet("resnet34", encoder_weights=None, classes=1, activation=None)
    model.load_state_dict(torch.load(os.path.join(os.path.dirname(__file__), "unet_model2.pth"), map_location=device))
    model.to(device).eval()
except RuntimeError as e:
    print(f"Error loading model: {e}")
    print("Trying with smaller batch size...")
    try:
        # Try loading with smaller memory footprint
        torch.cuda.empty_cache() if torch.cuda.is_available() else None
        gc.collect()
        model = smp.Unet("resnet18", encoder_weights=None, classes=1, activation=None)
        model.load_state_dict(torch.load(os.path.join(os.path.dirname(__file__), "unet_model2.pth"), map_location='cpu'))
        model.to('cpu').eval()
    except Exception as e:
        print(f"Failed to load model: {e}")
        model = None

# ------------------------------------------------------------------
# Helpers with memory management
# ------------------------------------------------------------------

def np_to_png_data_uri(arr: np.ndarray) -> str:
    _, buf = cv2.imencode(".png", arr)
    return "data:image/png;base64," + base64.b64encode(buf).decode()

def process_image(img_arr: np.ndarray) -> np.ndarray:
    try:
        print(f"[DEBUG] process_image received shape: {img_arr.shape}")
        h, w = img_arr.shape[:2]
        print(f"[DEBUG] h: {h}, w: {w}")
        ph, pw = 1024, 1024
        print(f"[DEBUG] Padding to: ({ph}, {pw})")
        padded = cv2.copyMakeBorder(img_arr, 0, ph - h, 0, pw - w, cv2.BORDER_CONSTANT)
        print(f"[DEBUG] process_image after padding: {padded.shape}")
        ten = transforms.ToTensor()(padded).unsqueeze(0)
        print(f"[DEBUG] Tensor shape before model: {ten.shape}")
        # Move to appropriate device
        if torch.cuda.is_available():
            print("[DEBUG] Using CUDA")
            ten = ten.cuda()
            torch.cuda.empty_cache()
        else:
            print("[DEBUG] Using CPU")
        with torch.no_grad():
            print("[DEBUG] Calling model...")
            prob = torch.sigmoid(model(ten))[0, 0].cpu().numpy()
            print(f"[DEBUG] Model output shape: {prob.shape}")
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        if ph * pw > 1000000:
            prob = cv2.resize(prob, (pw, ph))
            print(f"[DEBUG] Resized prob shape: {prob.shape}")
        mask = (prob > 0.5).astype(np.uint8)[:h, :w]
        print(f"[DEBUG] Final mask shape: {mask.shape}")
        return mask
    except Exception as e:
        print(f"Error in process_image: {e}")
        raise

def create_graph(mask: np.ndarray) -> nx.Graph:
    try:
        skel = skeletonize(mask).astype(np.uint8)
        G = nx.Graph()
        off = [(-1, -1), (-1, 0), (-1, 1), (0, -1), (0, 1), (1, -1), (1, 0), (1, 1)]
        h, w = skel.shape
        for y in range(1, h - 1):
            for x in range(1, w - 1):
                if skel[y, x]:
                    G.add_node((x, y))
                    for dx, dy in off:
                        if skel[y + dy, x + dx]:
                            G.add_edge((x, y), (x + dx, y + dy), weight=1)
        if not G.nodes:
            raise ValueError("No road pixels detected")
        return G.subgraph(max(nx.connected_components(G), key=len)).copy()
    except Exception as e:
        print(f"Error in create_graph: {e}")
        raise

def closest(G: nx.Graph, point: tuple) -> tuple:
    return min(G.nodes(), key=lambda n: (n[0] - point[0]) ** 2 + (n[1] - point[1]) ** 2)

# ------------------------------------------------------------------
# End‑points with error handling
# ------------------------------------------------------------------

@app.route("/api/convert-tiff", methods=["POST"])
def convert_tiff():
    try:
        file = request.files["image"]
        arr = tiff.imread(file.stream)
        
        # Handle different image formats
        if arr.ndim == 3 and arr.shape[0] <= 4:
            arr = np.transpose(arr, (1, 2, 0))
        if arr.dtype != np.uint8:
            arr = (arr - arr.min()) / (arr.ptp() + 1e-6) * 255
            arr = arr.astype(np.uint8)
            
        # Resize if image is too large
        if arr.size > 1000000:  # If more than 1M pixels
            scale = np.sqrt(1000000 / arr.size)
            new_size = (int(arr.shape[1] * scale), int(arr.shape[0] * scale))
            arr = cv2.resize(arr, new_size)
            
        data_uri = np_to_png_data_uri(cv2.cvtColor(arr, cv2.COLOR_RGB2BGR))
        
        # Clean up memory
        del arr
        gc.collect()
        
        return jsonify({"convertedImage": data_uri})
    except Exception as e:
        print(f"Error in convert_tiff: {e}")
        return jsonify({"error": str(e)}), 500

@app.route("/api/calculate-route", methods=["POST"])
def calculate_route():
    try:
        print("[DEBUG] Received request at /api/calculate-route")
        data = request.get_json()
        print(f"[DEBUG] data keys: {list(data.keys())}")
        img_b64 = data["image"].split(",")[1]
        print(f"[DEBUG] Decoded base64 image, length: {len(img_b64)}")
        img_bytes = base64.b64decode(img_b64)
        print(f"[DEBUG] Decoded image bytes, length: {len(img_bytes)}")
        img = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        print(f"[DEBUG] PIL image loaded, size: {img.size}")
        img = img.resize((1024, 1024), Image.LANCZOS)
        print(f"[DEBUG] PIL image resized to: {img.size}")
        img_arr = np.array(img)
        print(f"[DEBUG] Numpy array shape: {img_arr.shape}")
        del img
        gc.collect()
        print("[DEBUG] Calling process_image...")
        mask = process_image(img_arr)
        print(f"[DEBUG] Mask shape: {mask.shape}")
        print("[DEBUG] Calling create_graph...")
        G = create_graph(mask)
        print(f"[DEBUG] Graph created with {len(G.nodes())} nodes and {len(G.edges())} edges")
        start = (int(data["start"]["x"]), int(data["start"]["y"]))
        end = (int(data["end"]["x"]), int(data["end"]["y"]))
        print(f"[DEBUG] Start: {start}, End: {end}")
        p_start = closest(G, start)
        print(f"[DEBUG] Closest start: {p_start}")
        p_end = closest(G, end)
        print(f"[DEBUG] Closest end: {p_end}")
        path = nx.shortest_path(G, p_start, p_end, weight="weight")
        print(f"[DEBUG] Path length: {len(path)}")
        path_coords = [{"x": int(x), "y": int(y)} for x, y in path]
        dist = sum(
            np.hypot(path[i + 1][0] - path[i][0], path[i + 1][1] - path[i][1])
            for i in range(len(path) - 1)
        )
        print(f"[DEBUG] Distance: {dist}")
        del G, mask
        gc.collect()
        print("[DEBUG] Returning response")
        return jsonify({"path": path_coords, "distance": dist, "displayImage": data["image"]})
    except Exception as e:
        print(f"Error in calculate_route: {e}")
        return jsonify({"error": str(e)}), 400

if __name__ == "__main__":
    app.run(port=8080)
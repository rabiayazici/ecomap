import React, { useState, useRef } from 'react';

/*------------------------------------------------
  Back‑end base URL – picked from Vite env file
  (.env.development) so that it works in dev/production.
------------------------------------------------*/
const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:4444';

const SatelliteRoutePage: React.FC = () => {
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [startPoint, setStartPoint] = useState<{ x: number; y: number } | null>(null);
  const [endPoint, setEndPoint] = useState<{ x: number; y: number } | null>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [origSize, setOrigSize] = useState<{ w: number; h: number }>({ w: 0, h: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ---------- Image Upload ---------- */
  const handleImageUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return setError('No file selected');
    if (file.size > 10 * 1024 * 1024) return setError('Image size must be < 10 MB');

    setLoading(true);
    setError(null);

    try {
      const isTiff =
        file.type.toLowerCase() === 'image/tiff' ||
        file.name.toLowerCase().endsWith('.tif') ||
        file.name.toLowerCase().endsWith('.tiff');

      if (isTiff) {
        /* ––– TIFF ▸ PNG on Flask ––– */
        const fd = new FormData();
        fd.append('image', file);
        const res = await fetch(`${API_BASE}/api/satellite-route/convert-tiff`, { 
          method: 'POST', 
          body: fd,
          credentials: 'include',
          headers: {
            'Accept': 'application/json',
          }
        });
        if (!res.ok) throw new Error('TIFF conversion failed');
        const { convertedImage } = await res.json();
        if (!convertedImage) throw new Error('No image returned');
        loadImageToCanvas(convertedImage);
      } else {
        /* ––– JPEG / PNG directly ––– */
        const reader = new FileReader();
        reader.onload = e => loadImageToCanvas(e.target?.result as string);
        reader.onerror = () => setError('Failed to read file');
        reader.readAsDataURL(file);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to process image');
    } finally {
      setLoading(false);
    }
  };

  /* draw image to <canvas> & keep original size */
  const loadImageToCanvas = (dataUrl: string) => {
    const img = new Image();
    img.onload = () => {
      setSelectedImage(dataUrl);
      setStartPoint(null);
      setEndPoint(null);
      setOrigSize({ w: img.width, h: img.height });

      const canvas = canvasRef.current;
      if (!canvas) return;

      /* responsive scale */
      let { width, height } = img;
      const maxW = window.innerWidth * 0.9;
      const maxH = window.innerHeight * 0.7;
      if (width > maxW) {
        const r = maxW / width;
        width = maxW;
        height *= r;
      }
      if (height > maxH) {
        const r = maxH / height;
        height = maxH;
        width *= r;
      }

      canvas.width = width;
      canvas.height = height;
      canvas.getContext('2d')!.drawImage(img, 0, 0, width, height);
    };
    img.onerror = () => setError('Failed to load image');
    img.src = dataUrl;
  };

  /* helper to draw start/end dots */
  const drawPoint = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    color: string,
  ) => {
    ctx.beginPath();
    ctx.arc(x, y, 5, 0, 2 * Math.PI);
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = 'white';
    ctx.lineWidth = 2;
    ctx.stroke();
  };

  /* ---------- Canvas click ---------- */
  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!canvasRef.current || !selectedImage) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d')!;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const scaleX = origSize.w / canvas.width;
    const scaleY = origSize.h / canvas.height;
    const realX = x * scaleX;
    const realY = y * scaleY;

    const img = new Image();
    img.onload = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

      if (startPoint) drawPoint(ctx, startPoint.x / scaleX, startPoint.y / scaleY, 'red');
      if (endPoint) drawPoint(ctx, endPoint.x / scaleX, endPoint.y / scaleY, 'yellow');

      if (!startPoint) {
        setStartPoint({ x: realX, y: realY });
        drawPoint(ctx, x, y, 'red');
      } else if (!endPoint) {
        setEndPoint({ x: realX, y: realY });
        drawPoint(ctx, x, y, 'yellow');
      }
    };
    img.src = selectedImage;
  };

  /* ---------- Submit route ---------- */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedImage || !startPoint || !endPoint)
      return setError('Please select both start and end points');

    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`${API_BASE}/api/satellite-route/calculate`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ image: selectedImage, start: startPoint, end: endPoint }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to calculate route');

      const canvas = canvasRef.current;
      if (!canvas) return;
      const ctx = canvas.getContext('2d')!;
      const img = new Image();
      img.onload = () => {
        const drawW = canvas.width;
        const drawH = canvas.height;
        ctx.clearRect(0, 0, drawW, drawH);
        ctx.drawImage(img, 0, 0, drawW, drawH);

        if (startPoint)
          drawPoint(ctx, startPoint.x * (drawW / origSize.w), startPoint.y * (drawH / origSize.h), 'red');
        if (endPoint)
          drawPoint(ctx, endPoint.x * (drawW / origSize.w), endPoint.y * (drawH / origSize.h), 'yellow');

        ctx.strokeStyle = 'lime';
        ctx.lineWidth = 3;
        const pts = data.path;
        if (pts?.length) {
          const sX = drawW / origSize.w;
          const sY = drawH / origSize.h;
          ctx.beginPath();
          ctx.moveTo(pts[0].x * sX, pts[0].y * sY);
          for (let i = 1; i < pts.length; i++) ctx.lineTo(pts[i].x * sX, pts[i].y * sY);
          ctx.stroke();
        }
      };
      img.src = data.displayImage;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to calculate route');
    } finally {
      setLoading(false);
    }
  };

  /* ---------- JSX ---------- */
  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Eco Pathfinder – Real‑time Navigation</h1>

      <div className="bg-blue-50 p-4 rounded-lg mb-4">
        <h2 className="text-xl font-semibold mb-2">How It Works</h2>
        <ul className="list-disc list-inside space-y-2">
          <li>Upload a satellite image (TIFF, JPEG, PNG)</li>
          <li>
            Click start (<span className="text-red-600">red</span>) &amp; destination (<span className="text-yellow-600">yellow</span>)
          </li>
          <li>System calculates the optimal path</li>
        </ul>
      </div>

      <input
        type="file"
        accept="image/jpeg,image/png,image/tiff,.tif,.tiff"
        onChange={handleImageUpload}
        disabled={loading}
        className="mb-2"
      />
      {loading && <p className="text-gray-600">Processing…</p>}
      {error && <p className="text-red-600">{error}</p>}

      <div className="border-2 border-dashed border-gray-300 p-4 rounded-lg">
        <h2 className="text-xl font-semibold mb-2">Navigation Area</h2>
        {selectedImage ? (
          <>
            <canvas
              ref={canvasRef}
              onClick={handleCanvasClick}
              className="border border-gray-200 max-w-full h-auto"
            />
            <div className="mt-2 flex gap-4">
              <button
                onClick={() => {
                  setStartPoint(null);
                  setEndPoint(null);
                  selectedImage && loadImageToCanvas(selectedImage);
                }}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
              >
                Reset
              </button>
              {startPoint && endPoint && (
                <button
                  onClick={handleSubmit}
                  disabled={loading}
                  className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  Calculate Route
                </button>
              )}
            </div>
          </>
        ) : (
          <p className="text-center py-10 text-gray-500">Please upload an image to start navigation</p>
        )}
      </div>
    </div>
  );
};

export default SatelliteRoutePage; 
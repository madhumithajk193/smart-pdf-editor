
import { useState } from "react";

function RotatePdf({ pdfList = [], onRotateSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pages, setPages] = useState("");
    const [rotation, setRotation] = useState("90");
    const [outputFileName, setOutputFileName] =
        useState("rotated.pdf");

    const [message, setMessage] = useState("");
    const [rotatedFileName, setRotatedFileName] =
        useState("");
    const [isRotating, setIsRotating] = useState(false);

    // Deployed Render backend URL
    const API_URL = (
        import.meta.env.VITE_API_URL ||
        "https://smart-pdf-editor-backend.onrender.com"
    ).replace(/\/+$/, "");

    const handleRotate = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!pages.trim()) {
            setMessage("Please enter the pages to rotate.");
            return;
        }

        let fileName = outputFileName.trim() || "rotated.pdf";

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        if (/[<>:"/\\|?*\x00-\x1F]/.test(fileName)) {
            setMessage("Please enter a valid PDF filename.");
            return;
        }

        try {

            setIsRotating(true);
            setMessage("");
            setRotatedFileName("");

            const formData = new FormData();

            formData.append("pages", pages);
            formData.append("rotation", rotation);
            formData.append("outputFileName", fileName);

            const response = await fetch(
                `${API_URL}/api/pdfs/rotate/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(
                    result || `Rotation failed (${response.status}).`
                );
            }

            // Use the actual filename returned by the backend.
            const returnedFileName = result.trim();

            const actualFileName = returnedFileName
                ? returnedFileName
                    .replace(/\\/g, "/")
                    .split("/")
                    .pop()
                : fileName;

            setRotatedFileName(actualFileName);

            setMessage("PDF rotated successfully!");

            setPages("");

            if (onRotateSuccess) {
                onRotateSuccess();
            }

        } catch (error) {

            console.error("PDF rotation error:", error);

            setMessage(
                error.message || "Cannot connect to backend."
            );

            setRotatedFileName("");

        } finally {

            setIsRotating(false);

        }
    };

    const handleDownload = () => {

        if (!rotatedFileName) {
            return;
        }

        const downloadUrl =
            `${API_URL}/api/pdfs/download-merged/` +
            encodeURIComponent(rotatedFileName);

        window.open(
            downloadUrl,
            "_blank",
            "noopener,noreferrer"
        );

    };

    return (

        <div className="rotateContainer">

            <div className="rotateHeader">

                <h2>🔄 Rotate PDF</h2>

                <p>
                    Select a PDF and choose the pages
                    you want to rotate.
                </p>

            </div>

            <select
                value={selectedPdf}
                onChange={(e) => {
                    setSelectedPdf(e.target.value);
                    setMessage("");
                    setRotatedFileName("");
                }}
                disabled={isRotating}
            >

                <option value="">
                    Select PDF
                </option>

                {pdfList.map((pdf) => (

                    <option
                        key={pdf.id}
                        value={pdf.id}
                    >
                        {pdf.fileName}
                    </option>

                ))}

            </select>

            <br />
            <br />

            <input
                type="text"
                value={pages}
                onChange={(e) => setPages(e.target.value)}
                placeholder="Pages (Example: 1,2,4)"
                disabled={isRotating}
            />

            <br />
            <br />

            <select
                value={rotation}
                onChange={(e) => setRotation(e.target.value)}
                disabled={isRotating}
            >

                <option value="90">
                    Rotate 90°
                </option>

                <option value="180">
                    Rotate 180°
                </option>

                <option value="270">
                    Rotate 270°
                </option>

            </select>

            <br />
            <br />

            <input
                type="text"
                value={outputFileName}
                onChange={(e) => setOutputFileName(e.target.value)}
                placeholder="Output file name"
                disabled={isRotating}
            />

            <br />
            <br />

            <button
                onClick={handleRotate}
                disabled={isRotating}
            >
                {isRotating ? "Rotating..." : "🔄 Rotate PDF"}
            </button>

            {message && (
                <p role="status" aria-live="polite">
                    {message}
                </p>
            )}

            {rotatedFileName && (

                <div>

                    <p>
                        <b>{rotatedFileName}</b> is ready.
                    </p>

                    <button onClick={handleDownload}>
                        ⬇ Download Rotated PDF
                    </button>

                </div>

            )}

        </div>
    );
}

export default RotatePdf;


import { useState } from "react";

const API_URL =
    import.meta.env.VITE_API_URL ||
    "https://smart-pdf-editor-backend.onrender.com";

function Watermark({ pdfList, onWatermarkSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [watermarkText, setWatermarkText] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("watermarked.pdf");

    const [message, setMessage] = useState("");
    const [watermarkedFileName, setWatermarkedFileName] =
        useState("");

    const handleWatermark = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!watermarkText.trim()) {
            setMessage("Please enter watermark text.");
            return;
        }

        let fileName = outputFileName.trim();

        if (!fileName) {
            fileName = "watermarked.pdf";
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try {

            setMessage("Adding watermark...");
            setWatermarkedFileName("");

            const formData = new FormData();

            formData.append("watermarkText", watermarkText);
            formData.append("outputFileName", fileName);

            const response = await fetch(
                `${API_URL}/api/pdfs/watermark/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(
                    result || "Watermark operation failed."
                );
            }

            // Backend must return the actual saved filename.
            const actualFileName = result.trim();

            if (!actualFileName) {
                throw new Error("Backend returned an empty filename.");
            }

            setWatermarkedFileName(actualFileName);
            setMessage("Watermark added successfully!");

            if (onWatermarkSuccess) {
                onWatermarkSuccess();
            }

        } catch (error) {

            console.error("Watermark error:", error);

            setMessage(
                error.message || "Cannot connect to backend."
            );

            setWatermarkedFileName("");
        }
    };

    const handleDownload = async () => {

        if (!watermarkedFileName) {
            return;
        }

        try {

            const response = await fetch(
                `${API_URL}/api/pdfs/download-merged/${encodeURIComponent(watermarkedFileName)}`,
                {
                    method: "GET",
                    credentials: "include"
                }
            );

            if (!response.ok) {
                throw new Error("Unable to download watermarked PDF.");
            }

            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);

            const link = document.createElement("a");
            link.href = downloadUrl;
            link.download = watermarkedFileName;

            document.body.appendChild(link);
            link.click();
            link.remove();

            window.URL.revokeObjectURL(downloadUrl);

        } catch (error) {

            console.error("Download error:", error);
            setMessage(error.message);
        }
    };

    return (

        <div className="watermarkContainer">

            <div className="watermarkHeader">
                <h2>💧 Add Watermark</h2>
                <p>Select a PDF and add a watermark to it.</p>
            </div>

            <select
                value={selectedPdf}
                onChange={(e) => setSelectedPdf(e.target.value)}
            >
                <option value="">Select PDF</option>

                {pdfList.map((pdf) => (
                    <option key={pdf.id} value={pdf.id}>
                        {pdf.fileName}
                    </option>
                ))}
            </select>

            <br />
            <br />

            <input
                type="text"
                value={watermarkText}
                onChange={(e) => setWatermarkText(e.target.value)}
                placeholder="Enter watermark text"
            />

            <br />
            <br />

            <input
                type="text"
                value={outputFileName}
                onChange={(e) => setOutputFileName(e.target.value)}
                placeholder="Output file name"
            />

            <br />
            <br />

            <button onClick={handleWatermark}>
                💧 Add Watermark
            </button>

            {message && <p>{message}</p>}

            {watermarkedFileName && (
                <div>
                    <p>
                        <b>{watermarkedFileName}</b> is ready.
                    </p>

                    <button onClick={handleDownload}>
                        ⬇ Download Watermarked PDF
                    </button>
                </div>
            )}

        </div>
    );
}

export default Watermark;

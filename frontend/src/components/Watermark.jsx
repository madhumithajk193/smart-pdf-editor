import { useState } from "react";

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

            const formData = new FormData();

            formData.append(
                "watermarkText",
                watermarkText
            );

            formData.append(
                "outputFileName",
                fileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/watermark/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (response.ok) {

                setMessage(
                    "Watermark added successfully!"
                );

                setWatermarkedFileName(fileName);

                if (onWatermarkSuccess) {
                    onWatermarkSuccess();
                }

            } else {

                setMessage(
                    "Error adding watermark: " + result
                );

                setWatermarkedFileName("");
            }

        } catch (error) {

            console.error(error);

            setMessage(
                "Cannot connect to backend."
            );

            setWatermarkedFileName("");
        }
    };

    const handleDownload = () => {

        if (!watermarkedFileName) {
            return;
        }

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(watermarkedFileName)}`,
            "_blank"
        );
    };

    return (

        <div className="watermarkContainer">

            <div className="watermarkHeader">

                <h2>💧 Add Watermark</h2>

                <p>
                    Select a PDF and add a watermark
                    to it.
                </p>

            </div>

            <select
                value={selectedPdf}
                onChange={(e) =>
                    setSelectedPdf(e.target.value)
                }
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
                value={watermarkText}
                onChange={(e) =>
                    setWatermarkText(e.target.value)
                }
                placeholder="Enter watermark text"
            />

            <br />
            <br />

            <input
                type="text"
                value={outputFileName}
                onChange={(e) =>
                    setOutputFileName(e.target.value)
                }
                placeholder="Output file name"
            />

            <br />
            <br />

            <button onClick={handleWatermark}>
                💧 Add Watermark
            </button>

            {message && (
                <p>{message}</p>
            )}

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
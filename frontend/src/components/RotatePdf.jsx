import { useState } from "react";

function RotatePdf({ pdfList, onRotateSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pages, setPages] = useState("");
    const [rotation, setRotation] = useState("90");
    const [outputFileName, setOutputFileName] =
        useState("rotated.pdf");

    const [message, setMessage] = useState("");
    const [rotatedFileName, setRotatedFileName] =
        useState("");

    const handleRotate = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!pages.trim()) {
            setMessage(
                "Please enter the pages to rotate."
            );
            return;
        }

        let fileName = outputFileName.trim();

        if (!fileName) {
            fileName = "rotated.pdf";
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try {

            const formData = new FormData();

            formData.append("pages", pages);
            formData.append("rotation", rotation);
            formData.append(
                "outputFileName",
                fileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/rotate/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (response.ok) {

                setMessage(
                    "PDF rotated successfully!"
                );

                setRotatedFileName(fileName);

                setPages("");

                if (onRotateSuccess) {
                    onRotateSuccess();
                }

            } else {

                setMessage(
                    "Error rotating PDF: " + result
                );

                setRotatedFileName("");
            }

        } catch (error) {

            console.error(error);

            setMessage(
                "Cannot connect to backend."
            );

            setRotatedFileName("");
        }
    };

    const handleDownload = () => {

        if (!rotatedFileName) {
            return;
        }

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(rotatedFileName)}`,
            "_blank"
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
                value={pages}
                onChange={(e) =>
                    setPages(e.target.value)
                }
                placeholder="Pages (Example: 1,2,4)"
            />

            <br />
            <br />

            <select
                value={rotation}
                onChange={(e) =>
                    setRotation(e.target.value)
                }
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
                onChange={(e) =>
                    setOutputFileName(e.target.value)
                }
                placeholder="Output file name"
            />

            <br />
            <br />

            <button onClick={handleRotate}>
                🔄 Rotate PDF
            </button>

            {message && (
                <p>{message}</p>
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
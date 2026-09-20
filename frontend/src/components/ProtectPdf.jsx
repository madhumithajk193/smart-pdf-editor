import { useState } from "react";

function ProtectPdf({ pdfList, onProtectSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [password, setPassword] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("protected.pdf");

    const [message, setMessage] = useState("");
    const [protectedFileName, setProtectedFileName] =
        useState("");

    const handleProtect = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!password.trim()) {
            setMessage("Please enter a password.");
            return;
        }

        let fileName = outputFileName.trim();

        if (!fileName) {
            fileName = "protected.pdf";
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try {

            const formData = new FormData();

            formData.append("password", password);

            formData.append(
                "outputFileName",
                fileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/protect/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (response.ok) {

                setMessage(
                    "PDF protected successfully!"
                );

                setProtectedFileName(fileName);

                if (onProtectSuccess) {
                    onProtectSuccess();
                }

            } else {

                setMessage(
                    "Error protecting PDF: " + result
                );

                setProtectedFileName("");
            }

        } catch (error) {

            console.error(error);

            setMessage(
                "Cannot connect to backend."
            );

            setProtectedFileName("");
        }
    };

    const handleDownload = () => {

        if (!protectedFileName) {
            return;
        }

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(protectedFileName)}`,
            "_blank"
        );
    };

    return (

        <div className="protectContainer">

            <div className="protectHeader">

                <h2>🔐 Protect PDF</h2>

                <p>
                    Add a password to protect your PDF.
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
                type="password"
                value={password}
                onChange={(e) =>
                    setPassword(e.target.value)
                }
                placeholder="Enter password"
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

            <button onClick={handleProtect}>
                🔐 Protect PDF
            </button>

            {message && (
                <p>{message}</p>
            )}

            {protectedFileName && (

                <div>

                    <p>
                        <b>{protectedFileName}</b> is ready.
                    </p>

                    <button onClick={handleDownload}>
                        ⬇ Download Protected PDF
                    </button>

                </div>

            )}

        </div>
    );
}

export default ProtectPdf;
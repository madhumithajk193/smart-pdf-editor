import { useState, useEffect } from "react";

function PdfToWord({ pdfList = [] }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("converted.docx");

    const [message, setMessage] = useState("");
    const [downloadReady, setDownloadReady] =
        useState(false);

    useEffect(() => {
        if (pdfList.length > 0 && !selectedPdf) {
            setSelectedPdf(String(pdfList[0].id));
        }
    }, [pdfList, selectedPdf]);

    const handleConvert = async () => {

        if (!selectedPdf) {
            setMessage("❌ Please select a PDF");
            return;
        }

        if (!outputFileName.trim()) {
            setMessage("❌ Please enter an output file name");
            return;
        }

        try {

            setMessage("⏳ Converting PDF to Word...");
            setDownloadReady(false);

            const response = await fetch(
                `http://localhost:8080/api/pdfs/pdf-to-word/${selectedPdf}?outputFileName=${encodeURIComponent(
                    outputFileName.trim()
                )}`,
                {
                    method: "POST"
                }
            );

            if (!response.ok) {

                const errorText = await response.text();

                throw new Error(
                    errorText || "Conversion failed"
                );
            }

            const result = await response.text();

            console.log("Backend response:", result);

            setMessage(
                "✅ PDF converted to Word successfully!"
            );

            setDownloadReady(true);

        } catch (error) {

            console.error(
                "PDF to Word error:",
                error
            );

            setMessage(
                "❌ Conversion failed: " +
                error.message
            );

            setDownloadReady(false);
        }
    };

    return (
        <div className="featureContainer">

            <h2>📄 PDF to Word</h2>

            <p>
                Convert your PDF document into an editable
                Word document.
            </p>

            {/* PDF SELECT */}

            <div className="formGroup">

                <label>
                    📄 Select PDF
                </label>

                <select
                    value={selectedPdf}
                    onChange={(e) =>
                        setSelectedPdf(e.target.value)
                    }
                >

                    <option value="">
                        -- Select PDF --
                    </option>

                    {Array.isArray(pdfList) &&
                        pdfList.map((pdf) => (

                            <option
                                key={pdf.id}
                                value={pdf.id}
                            >
                                {pdf.fileName || pdf.file_name}
                            </option>

                        ))
                    }

                </select>

            </div>

            {/* OUTPUT FILE NAME */}

            <div className="formGroup">

                <label>
                    📝 Output File Name
                </label>

                <input
                    type="text"
                    value={outputFileName}
                    onChange={(e) =>
                        setOutputFileName(e.target.value)
                    }
                    placeholder="converted.docx"
                />

            </div>

            {/* CONVERT */}

            <button
                type="button"
                className="primaryButton"
                onClick={handleConvert}
            >
                📄 Convert to Word
            </button>

            {/* MESSAGE */}

            {message && (

                <div className="successMessage">

                    {message}

                    {downloadReady && (

                        <div
                            style={{
                                marginTop: "15px"
                            }}
                        >

                            <a
                                href={
                                    `http://localhost:8080/api/pdfs/download-word/${encodeURIComponent(
                                        outputFileName.trim()
                                    )}`
                                }
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download Word
                            </a>

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}

export default PdfToWord;
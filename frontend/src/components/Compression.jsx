import { useState } from "react";

function Compression({ pdfList, onCompressionSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("compressed.pdf");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [downloadReady, setDownloadReady] = useState(false);


    const compressPdf = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name.");
            return;
        }

        setLoading(true);

        try {

            const formData = new URLSearchParams();

            formData.append(
                "outputFileName",
                outputFileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/compress/${selectedPdf}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },
                    body: formData
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(result);
            }

            setMessage(result);
            setDownloadReady(true);

            if (onCompressionSuccess) {
                onCompressionSuccess();
            }

        } catch (err) {

            console.error(
                "Compression error:",
                err
            );

            setError(
                err.message ||
                "Error compressing PDF."
            );

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    🗜️ Compress PDF
                </h1>

                <p>
                    Reduce the size of your PDF document.
                </p>

            </div>


            {/* Select PDF */}

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
                        -- Select a PDF --
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

            </div>


            {/* Output file */}

            <div className="formGroup">

                <label>
                    💾 Output File Name
                </label>

                <input
                    type="text"
                    value={outputFileName}
                    onChange={(e) =>
                        setOutputFileName(e.target.value)
                    }
                    placeholder="compressed.pdf"
                />

            </div>


            {/* Compress button */}

            <button
                className="primaryButton"
                onClick={compressPdf}
                disabled={loading}
            >

                {loading
                    ? "🗜️ Compressing..."
                    : "🗜️ Compress PDF"
                }

            </button>


            {/* Success */}

            {message && (

                <div className="successMessage">

                    ✅ {message}

                    {downloadReady && (

                        <div
                            style={{
                                marginTop: "15px"
                            }}
                        >

                            <a
                                href={`http://localhost:8080/api/pdfs/download-file/${outputFileName}`}
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download Compressed PDF
                            </a>

                        </div>

                    )}

                </div>

            )}


            {/* Error */}

            {error && (

                <div className="errorMessage">

                    ❌ {error}

                </div>

            )}

        </div>

    );

}

export default Compression;
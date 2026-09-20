import { useState } from "react";

function PdfToImage({ pdfList, onConversionSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [outputFolder, setOutputFolder] =
        useState("pdf_images");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [downloadReady, setDownloadReady] = useState(false);


    const convertPdfToImages = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (!outputFolder.trim()) {
            setError("Please enter an output folder.");
            return;
        }

        setLoading(true);

        try {

            const formData = new URLSearchParams();

            formData.append(
                "outputFolder",
                outputFolder
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/pdf-to-images/${selectedPdf}`,
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

            if (onConversionSuccess) {
                onConversionSuccess();
            }

        } catch (err) {

            console.error(
                "PDF to image error:",
                err
            );

            setError(
                err.message ||
                "Error converting PDF to images."
            );

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    🖼️ PDF to Images
                </h1>

                <p>
                    Convert PDF pages into image files.
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


            {/* Output Folder */}

            <div className="formGroup">

                <label>
                    📁 Output Folder
                </label>

                <input
                    type="text"
                    value={outputFolder}
                    onChange={(e) =>
                        setOutputFolder(e.target.value)
                    }
                    placeholder="pdf_images"
                />

            </div>


            {/* Convert */}

            <button
                className="primaryButton"
                onClick={convertPdfToImages}
                disabled={loading}
            >

                {loading
                    ? "🖼️ Converting..."
                    : "🖼️ Convert PDF to Images"
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
                                href={`http://localhost:8080/api/pdfs/download-images?folder=${encodeURIComponent(outputFolder)}`}
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download Images
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

export default PdfToImage;
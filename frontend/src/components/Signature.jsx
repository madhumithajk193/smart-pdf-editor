import { useState } from "react";

function Signature({ pdfList, onSignatureSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [signature, setSignature] = useState(null);

    const [pageNumber, setPageNumber] = useState(1);
    const [x, setX] = useState(100);
    const [y, setY] = useState(500);
    const [width, setWidth] = useState(150);
    const [height, setHeight] = useState(60);

    const [outputFileName, setOutputFileName] =
        useState("signed_test.pdf");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [downloadReady, setDownloadReady] = useState(false);

    const handleSignatureChange = (event) => {

        const file = event.target.files[0];

        if (!file) {
            setSignature(null);
            return;
        }

        // Allow image files only
        if (!file.type.startsWith("image/")) {
            setError("Please select an image file.");
            setSignature(null);
            return;
        }

        setError("");
        setSignature(file);
    };


    const addSignature = async () => {

        setMessage("");
        setError("");

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (!signature) {
            setError("Please select a signature image.");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name.");
            return;
        }

        try {

            setLoading(true);

            const formData = new FormData();

            formData.append("signature", signature);
            formData.append("pageNumber", pageNumber);
            formData.append("x", x);
            formData.append("y", y);
            formData.append("width", width);
            formData.append("height", height);
            formData.append(
                "outputFileName",
                outputFileName
            );


            const response = await fetch(
                `http://localhost:8080/api/pdfs/signature/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData
                }
            );


            const result = await response.text();


            if (!response.ok) {
                throw new Error(result || "Failed to add signature.");
            }

            setMessage(result);
            setDownloadReady(true);

            if (onSignatureSuccess) {
                onSignatureSuccess();
            }

        } catch (err) {

            console.error("Signature error:", err);

            setError(
                "Error: " +
                (err.message || "Something went wrong.")
            );

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureTool">

            <div className="toolHeader">

                <h2>✍️ Add Signature</h2>

                <p>
                    Add a signature image to any page of your PDF.
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
                        -- Select PDF --
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


            {/* Signature Image */}

            <div className="formGroup">

                <label>
                    ✍️ Select Signature Image
                </label>

                <input
                    type="file"
                    accept="image/*"
                    onChange={handleSignatureChange}
                />

                {signature && (

                    <p className="fileInfo">
                        Selected: {signature.name}
                    </p>

                )}

            </div>


            {/* Page Number */}

            <div className="formGroup">

                <label>
                    📄 Page Number
                </label>

                <input
                    type="number"
                    min="1"
                    value={pageNumber}
                    onChange={(e) =>
                        setPageNumber(e.target.value)
                    }
                />

            </div>


            {/* Position */}

            <div className="positionSection">

                <h3>
                    📌 Signature Position
                </h3>

                <p className="coordinateInfo">
                    PDF coordinates start from the
                    bottom-left corner of the page.
                </p>


                <div className="coordinateGrid">

                    <div className="formGroup">

                        <label>
                            X Position
                        </label>

                        <input
                            type="number"
                            value={x}
                            onChange={(e) =>
                                setX(e.target.value)
                            }
                        />

                    </div>


                    <div className="formGroup">

                        <label>
                            Y Position
                        </label>

                        <input
                            type="number"
                            value={y}
                            onChange={(e) =>
                                setY(e.target.value)
                            }
                        />

                    </div>

                </div>

            </div>


            {/* Size */}

            <div className="positionSection">

                <h3>
                    📐 Signature Size
                </h3>


                <div className="coordinateGrid">

                    <div className="formGroup">

                        <label>
                            Width
                        </label>

                        <input
                            type="number"
                            min="1"
                            value={width}
                            onChange={(e) =>
                                setWidth(e.target.value)
                            }
                        />

                    </div>


                    <div className="formGroup">

                        <label>
                            Height
                        </label>

                        <input
                            type="number"
                            min="1"
                            value={height}
                            onChange={(e) =>
                                setHeight(e.target.value)
                            }
                        />

                    </div>

                </div>

            </div>


            {/* Output File */}

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
                    placeholder="signed_test.pdf"
                />

            </div>


            {/* Button */}

            <button
                className="primaryButton"
                onClick={addSignature}
                disabled={loading}
            >

                {loading
                    ? "✍️ Adding Signature..."
                    : "✍️ Add Signature to PDF"
                }

            </button>


            {/* Success */}
            {message && (

                <div className="successMessage">

                    ✅ {message}

                    {downloadReady && (

                        <div style={{ marginTop: "15px" }}>

                            <a
                                href={`http://localhost:8080/api/pdfs/download-file/${outputFileName}`}
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download Signed PDF
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

export default Signature;
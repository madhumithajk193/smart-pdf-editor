import { useState } from "react";

function ReorderPages({ pdfList, onReorderSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pageOrder, setPageOrder] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("reordered.pdf");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [downloadReady, setDownloadReady] = useState(false);


    const handleReorder = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (!pageOrder.trim()) {
            setError("Please enter the page order.");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name.");
            return;
        }

        setLoading(true);

        try {

            const params = new URLSearchParams();

            params.append("pageOrder", pageOrder);
            params.append("outputFileName", outputFileName);

            const response = await fetch(
                `http://localhost:8080/api/pdfs/reorder/${selectedPdf}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },
                    body: params
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(result);
            }

            setMessage(result);
            setDownloadReady(true);

            if (onReorderSuccess) {
                onReorderSuccess();
            }

        } catch (err) {

            console.error(
                "Reorder PDF error:",
                err
            );

            setError(
                err.message ||
                "Error reordering PDF pages."
            );

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    🔢 Reorder PDF Pages
                </h1>

                <p>
                    Change the order of pages in your PDF.
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


            {/* Page Order */}

            <div className="formGroup">

                <label>
                    🔢 Page Order
                </label>

                <input
                    type="text"
                    value={pageOrder}
                    onChange={(e) =>
                        setPageOrder(e.target.value)
                    }
                    placeholder="Example: 3,1,2,5,4"
                />

                <small>
                    Enter the page numbers in the order
                    you want them to appear.
                </small>

            </div>


            {/* Output filename */}

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
                    placeholder="reordered.pdf"
                />

            </div>


            {/* Reorder button */}

            <button
                className="primaryButton"
                onClick={handleReorder}
                disabled={loading}
            >

                {loading
                    ? "🔄 Reordering Pages..."
                    : "🔄 Reorder Pages"
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
                                ⬇️ Download Reordered PDF
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

export default ReorderPages;
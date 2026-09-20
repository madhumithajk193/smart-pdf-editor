import { useState } from "react";

function Metadata({ pdfList, onMetadataSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [title, setTitle] = useState("");
    const [author, setAuthor] = useState("");
    const [subject, setSubject] = useState("");
    const [keywords, setKeywords] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("metadata_updated.pdf");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [downloadReady, setDownloadReady] = useState(false);


    const editMetadata = async () => {

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

            formData.append("title", title);
            formData.append("author", author);
            formData.append("subject", subject);
            formData.append("keywords", keywords);
            formData.append(
                "outputFileName",
                outputFileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/metadata/${selectedPdf}`,
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

            if (onMetadataSuccess) {
                onMetadataSuccess();
            }

        } catch (err) {

            console.error(
                "Metadata error:",
                err
            );

            setError(
                err.message ||
                "Error editing PDF metadata."
            );

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    📝 Edit PDF Metadata
                </h1>

                <p>
                    Update the title, author, subject and
                    keywords of your PDF.
                </p>

            </div>


            {/* PDF Selection */}

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


            {/* Title */}

            <div className="formGroup">

                <label>
                    📌 Title
                </label>

                <input
                    type="text"
                    value={title}
                    onChange={(e) =>
                        setTitle(e.target.value)
                    }
                    placeholder="Enter PDF title"
                />

            </div>


            {/* Author */}

            <div className="formGroup">

                <label>
                    👤 Author
                </label>

                <input
                    type="text"
                    value={author}
                    onChange={(e) =>
                        setAuthor(e.target.value)
                    }
                    placeholder="Enter author name"
                />

            </div>


            {/* Subject */}

            <div className="formGroup">

                <label>
                    📚 Subject
                </label>

                <input
                    type="text"
                    value={subject}
                    onChange={(e) =>
                        setSubject(e.target.value)
                    }
                    placeholder="Enter PDF subject"
                />

            </div>


            {/* Keywords */}

            <div className="formGroup">

                <label>
                    🔑 Keywords
                </label>

                <input
                    type="text"
                    value={keywords}
                    onChange={(e) =>
                        setKeywords(e.target.value)
                    }
                    placeholder="Example: PDF, document, project"
                />

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
                    placeholder="metadata_updated.pdf"
                />

            </div>


            {/* Button */}

            <button
                className="primaryButton"
                onClick={editMetadata}
                disabled={loading}
            >

                {loading
                    ? "📝 Updating Metadata..."
                    : "📝 Update PDF Metadata"
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
                                ⬇️ Download Updated PDF
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

export default Metadata;
import { useState } from "react";

function PdfTable({ pdfList, onDelete }) {

    const [message, setMessage] = useState("");

    const handleDelete = async (id) => {

        const confirmDelete = window.confirm(
            "Are you sure you want to delete this PDF?"
        );

        if (!confirmDelete) {
            return;
        }

        try {

            const response = await fetch(
                `http://localhost:8080/api/pdfs/${id}`,
                {
                    method: "DELETE"
                }
            );

            if (response.ok) {

                setMessage("PDF deleted successfully!");

                if (onDelete) {
                    onDelete();
                }

            } else {

                setMessage("Delete failed.");

            }

        } catch (error) {

            console.error(error);

            setMessage("Cannot connect to backend.");

        }

    };

    const handleView = (id) => {

        window.open(
            `http://localhost:8080/api/pdfs/view/${id}`,
            "_blank"
        );

    };

    const handleDownload = (id) => {

        window.open(
            `http://localhost:8080/api/pdfs/download/${id}`,
            "_blank"
        );

    };

    return (

        <div className="pdfLibrary">

            {/* Header */}

            <div className="pdfLibraryHeader">

                <div className="pdfLibraryIcon">
                    📚
                </div>

                <div>
                    <h2>My PDF Documents</h2>

                    <p>
                        Manage, preview, download and delete your PDF files.
                    </p>
                </div>

            </div>


            {/* Message */}

            {message && (

                <div className="pdfMessage">
                    <span>✓</span>
                    {message}
                </div>

            )}


            {/* Empty State */}

            {pdfList.length === 0 ? (

                <div className="emptyPdfState">

                    <div className="emptyPdfIcon">
                        📄
                    </div>

                    <h3>No PDFs uploaded yet</h3>

                    <p>
                        Upload a PDF to see it here.
                    </p>

                </div>

            ) : (

                <div className="pdfCards">

                    {pdfList.map((pdf) => (

                        <div
                            className="pdfCard"
                            key={pdf.id}
                        >

                            {/* PDF Icon */}

                            <div className="pdfIcon">
                                📄
                            </div>


                            {/* PDF Information */}

                            <div className="pdfInfo">

                                <h3 title={pdf.fileName}>
                                    {pdf.fileName}
                                </h3>

                                <div className="pdfDetails">

                                    <span>
                                        PDF Document
                                    </span>

                                    <span className="dot">
                                        •
                                    </span>

                                    <span>
                                        ID: {pdf.id}
                                    </span>

                                </div>

                            </div>


                            {/* Actions */}

                            <div className="pdfActions">

                                <button
                                    className="viewPdfButton"
                                    onClick={() =>
                                        handleView(pdf.id)
                                    }
                                >
                                    👁 View
                                </button>

                                <button
                                    className="downloadPdfButton"
                                    onClick={() =>
                                        handleDownload(pdf.id)
                                    }
                                >
                                    ⬇ Download
                                </button>

                                <button
                                    className="deletePdfButton"
                                    onClick={() =>
                                        handleDelete(pdf.id)
                                    }
                                >
                                    🗑 Delete
                                </button>

                            </div>

                        </div>

                    ))}

                </div>

            )}

        </div>

    );

}

export default PdfTable;
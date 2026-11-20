
import { useState } from "react";

const API_URL =
    import.meta.env.VITE_API_URL ||
    "https://smart-pdf-editor-backend.onrender.com";

function DeletePdf({ pdfList, onDelete }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [loading, setLoading] = useState(false);

    const handleDelete = async () => {

        if (!selectedPdf) {
            alert("Please select a PDF.");
            return;
        }

        const selectedFile = pdfList.find(
            (pdf) => String(pdf.id) === String(selectedPdf)
        );

        if (!selectedFile) {
            alert("PDF not found.");
            return;
        }

        const confirmDelete = window.confirm(
            `Are you sure you want to delete "${selectedFile.fileName}"?`
        );

        if (!confirmDelete) {
            return;
        }

        try {

            setLoading(true);

            const response = await fetch(
                `${API_URL}/api/pdfs/${selectedPdf}`,
                {
                    method: "DELETE",
                    credentials: "include"
                }
            );

            const message = await response.text();

            if (response.ok) {

                alert("PDF deleted successfully!");

                setSelectedPdf("");

                if (onDelete) {
                    onDelete();
                }

            } else {

                alert(
                    message || "Failed to delete PDF."
                );

            }

        } catch (error) {

            console.error("Delete PDF error:", error);

            alert("Cannot connect to backend. Please try again.");

        } finally {

            setLoading(false);

        }
    };

    return (

        <div className="deleteContainer">

            <div className="deleteHeader">

                <h2>🗑️ Delete PDF</h2>

                <p>
                    Select a PDF and delete it permanently.
                </p>

            </div>

            <select
                value={selectedPdf}
                onChange={(e) =>
                    setSelectedPdf(e.target.value)
                }
                disabled={loading}
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

            <button
                className="deleteButton"
                onClick={handleDelete}
                disabled={loading}
            >
                {loading ? "Deleting..." : "🗑️ Delete PDF"}
            </button>

        </div>

    );
}

export default DeletePdf;

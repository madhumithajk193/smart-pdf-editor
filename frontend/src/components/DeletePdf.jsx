import { useState } from "react";

function DeletePdf({ pdfList, onDelete }) {

    const [selectedPdf, setSelectedPdf] = useState("");

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

            const response = await fetch(
                `http://localhost:8080/api/pdfs/${selectedPdf}`,
                {
                    method: "DELETE"
                }
            );

            if (response.ok) {

                alert("PDF deleted successfully!");

                setSelectedPdf("");

                if (onDelete) {
                    onDelete();
                }

            } else {

                const message = await response.text();

                alert(
                    message || "Failed to delete PDF."
                );

            }

        } catch (error) {

            console.error(error);

            alert("Cannot connect to backend.");

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
            >
                🗑️ Delete PDF
            </button>

        </div>

    );

}

export default DeletePdf;
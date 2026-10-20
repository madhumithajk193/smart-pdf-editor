
import { useState } from "react";

function Merge({ pdfList = [], onMergeSuccess }) {
    const [selectedPdfs, setSelectedPdfs] = useState([]);
    const [outputFileName, setOutputFileName] = useState("merged.pdf");
    const [message, setMessage] = useState("");
    const [mergedFileName, setMergedFileName] = useState("");
    const [isMerging, setIsMerging] = useState(false);

    // Use the deployed backend URL in production.
    // Set VITE_API_URL in your frontend deployment environment.
    const API_URL = (
        import.meta.env.VITE_API_URL || "http://localhost:8080"
    ).replace(/\/+$/, "");

    const handleSelectionChange = (event) => {
        const values = Array.from(
            event.target.selectedOptions,
            (option) => option.value
        );

        setSelectedPdfs(values);
        setMessage("");
        setMergedFileName("");
    };

    const handleMerge = async () => {
        if (selectedPdfs.length < 2) {
            setMessage("Please select at least two different PDFs.");
            return;
        }

        let fileName = outputFileName.trim() || "merged.pdf";

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        // Avoid sending an unsafe output filename.
        if (/[<>:"/\\|?*\x00-\x1F]/.test(fileName)) {
            setMessage("Please enter a valid PDF filename.");
            return;
        }

        const formData = new FormData();

        // Send EVERY selected PDF ID, preserving the selected order.
        formData.append("pdfIds", selectedPdfs.join(","));
        formData.append("outputFileName", fileName);

        setIsMerging(true);
        setMessage("");
        setMergedFileName("");

        try {
            const response = await fetch(
                `${API_URL}/api/pdfs/merge`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(
                    result || `Merge failed (${response.status}).`
                );
            }

            setMergedFileName(fileName);
            setMessage(
                `Successfully merged ${selectedPdfs.length} PDFs!`
            );

            if (onMergeSuccess) {
                onMergeSuccess();
            }
        } catch (error) {
            console.error("PDF merge error:", error);
            setMessage(
                error.message || "Unable to merge PDFs. Please try again."
            );
        } finally {
            setIsMerging(false);
        }
    };

    const handleDownload = () => {
        if (!mergedFileName) return;

        const downloadUrl =
            `${API_URL}/api/pdfs/download-merged/` +
            encodeURIComponent(mergedFileName);

        window.open(downloadUrl, "_blank", "noopener,noreferrer");
    };

    return (
        <div className="uploadBox">
            <h2>Merge PDFs</h2>

            <p>
                Select multiple PDFs. Hold Ctrl on Windows while clicking
                to select several files.
            </p>

            <label htmlFor="pdf-selection">
                Choose PDFs to merge
            </label>

            <select
                id="pdf-selection"
                multiple
                size={Math.min(Math.max(pdfList.length, 3), 8)}
                value={selectedPdfs}
                onChange={handleSelectionChange}
                disabled={isMerging}
                style={{
                    width: "100%",
                    minHeight: "120px"
                }}
            >
                {pdfList.map((pdf) => (
                    <option
                        key={pdf.id}
                        value={String(pdf.id)}
                    >
                        {pdf.fileName}
                    </option>
                ))}
            </select>

            <p>
                Selected: {selectedPdfs.length} PDF(s)
            </p>

            <label htmlFor="merged-file-name">
                Output filename
            </label>

            <input
                id="merged-file-name"
                type="text"
                value={outputFileName}
                onChange={(event) =>
                    setOutputFileName(event.target.value)
                }
                placeholder="merged.pdf"
                disabled={isMerging}
            />

            <br />
            <br />

            <button
                onClick={handleMerge}
                disabled={isMerging || selectedPdfs.length < 2}
            >
                {isMerging ? "Merging..." : "Merge Selected PDFs"}
            </button>

            {message && (
                <p role="status" aria-live="polite">
                    {message}
                </p>
            )}

            {mergedFileName && (
                <div>
                    <p>
                        <b>{mergedFileName}</b> is ready to download.
                    </p>

                    <button onClick={handleDownload}>
                        ⬇ Download Merged PDF
                    </button>
                </div>
            )}
        </div>
    );
}

export default Merge;

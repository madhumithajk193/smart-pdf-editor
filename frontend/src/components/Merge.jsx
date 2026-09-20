import { useState } from "react";

function Merge({ pdfList, onMergeSuccess }) {

    const [firstPdf, setFirstPdf] = useState("");
    const [secondPdf, setSecondPdf] = useState("");
    const [outputFileName, setOutputFileName] = useState("merged.pdf");
    const [message, setMessage] = useState("");
    const [mergedFileName, setMergedFileName] = useState("");

    const handleMerge = async () => {

        if (!firstPdf || !secondPdf) {
            setMessage("Please select two PDFs.");
            return;
        }

        if (firstPdf === secondPdf) {
            setMessage("Please select two different PDFs.");
            return;
        }

        let fileName = outputFileName.trim();

        if (!fileName) {
            fileName = "merged.pdf";
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        const formData = new FormData();

        formData.append(
            "pdfIds",
            `${firstPdf},${secondPdf}`
        );

        formData.append(
            "outputFileName",
            fileName
        );

        try {

            const response = await fetch(
                "http://localhost:8080/api/pdfs/merge",
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (response.ok) {

                setMessage("PDFs merged successfully!");

                setMergedFileName(fileName);

                if (onMergeSuccess) {
                    onMergeSuccess();
                }

                console.log(result);

            } else {

                setMessage(result);
                setMergedFileName("");

            }

        } catch (error) {

            console.error(error);

            setMessage("Cannot connect to backend.");

        }

    };

    const handleDownload = () => {

        if (!mergedFileName) {
            return;
        }

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(mergedFileName)}`,
            "_blank"
        );

    };

    return (

        <div className="uploadBox">

            <h2>Merge PDFs</h2>

            <select
                value={firstPdf}
                onChange={(e) => setFirstPdf(e.target.value)}
            >

                <option value="">
                    Select First PDF
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

            <select
                value={secondPdf}
                onChange={(e) => setSecondPdf(e.target.value)}
            >

                <option value="">
                    Select Second PDF
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

            <input
                type="text"
                value={outputFileName}
                onChange={(e) =>
                    setOutputFileName(e.target.value)
                }
                placeholder="Output File Name"
            />

            <br />
            <br />

            <button onClick={handleMerge}>
                Merge PDF
            </button>

            {message && (
                <p>{message}</p>
            )}

            {mergedFileName && (

                <div>

                    <p>
                        <b>{mergedFileName}</b> is ready.
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
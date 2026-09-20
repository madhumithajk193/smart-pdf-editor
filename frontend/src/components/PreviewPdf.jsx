function PreviewPdf({ pdf }) {

    if (!pdf) {
        return (
            <div className="previewContainer">
                <div className="previewEmpty">
                    <div className="previewIcon">📄</div>

                    <h2>Select a PDF</h2>

                    <p>
                        Choose a PDF from your library to preview it.
                    </p>
                </div>
            </div>
        );
    }

    const handleOpenPreview = () => {

        window.open(
            `http://localhost:8080/api/pdfs/view/${pdf.id}`,
            "_blank"
        );

    };

    return (

        <div className="previewContainer">

            <div className="previewHeader">

                <div>
                    <h2>👁 PDF Preview</h2>

                    <p>
                        Preview your PDF document
                    </p>
                </div>

                <button
                    className="openPreviewButton"
                    onClick={handleOpenPreview}
                >
                    ↗ Open Full Preview
                </button>

            </div>

            <div className="previewFileInfo">

                <div className="previewFileIcon">
                    📄
                </div>

                <div>

                    <h3>{pdf.fileName}</h3>

                    <p>
                        PDF Document • ID: {pdf.id}
                    </p>

                </div>

            </div>

            <div className="pdfViewer">

                <iframe
                    src={`http://localhost:8080/api/pdfs/view/${pdf.id}`}
                    title={pdf.fileName}
                    width="100%"
                    height="650"
                />

            </div>

        </div>

    );
}

export default PreviewPdf;
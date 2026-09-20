import React, { useEffect, useRef, useState } from "react";
import { Document, Page, pdfjs } from "react-pdf";

import "react-pdf/dist/Page/AnnotationLayer.css";
import "react-pdf/dist/Page/TextLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc =
    `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.mjs`;

const API_BASE_URL = "http://localhost:8080";

function PdfLinks({ pdfList = [] }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pageNumber, setPageNumber] = useState(1);
    const [numPages, setNumPages] = useState(0);

    const [url, setUrl] = useState("");
    const [outputFileName, setOutputFileName] = useState("");

    const [pdfUrl, setPdfUrl] = useState(null);

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const previewRef = useRef(null);

    const [isSelecting, setIsSelecting] = useState(false);

    const [selection, setSelection] = useState({
        x: 0,
        y: 0,
        width: 0,
        height: 0
    });

    const [startPoint, setStartPoint] = useState({
        x: 0,
        y: 0
    });


    // ============================================================
    // PDF ID
    // ============================================================

    const getPdfId = (pdf) => {

        if (typeof pdf === "number") {
            return pdf;
        }

        if (typeof pdf === "string") {
            return pdf;
        }

        return pdf?.id;
    };


    // ============================================================
    // PDF NAME
    // ============================================================

    const getPdfName = (pdf) => {

        if (typeof pdf === "string") {
            return pdf;
        }

        return (
            pdf?.fileName ||
            pdf?.filename ||
            pdf?.name ||
            `PDF ${pdf?.id}`
        );
    };


    // ============================================================
    // LOAD PDF
    // ============================================================

    useEffect(() => {

        if (!selectedPdf) {

            setPdfUrl(null);
            setNumPages(0);
            setPageNumber(1);

            return;
        }

        const newUrl =
            `${API_BASE_URL}/api/pdfs/view/${selectedPdf}`;

        setPdfUrl(newUrl);

        setPageNumber(1);

        setSelection({
            x: 0,
            y: 0,
            width: 0,
            height: 0
        });

        setError("");
        setMessage("");

    }, [selectedPdf]);


    // ============================================================
    // PDF LOAD SUCCESS
    // ============================================================

    const onDocumentLoadSuccess = ({
                                       numPages
                                   }) => {

        setNumPages(numPages);
        setPageNumber(1);
    };


    // ============================================================
    // PDF LOAD ERROR
    // ============================================================

    const onDocumentLoadError = (error) => {

        console.error(
            "PDF preview error:",
            error
        );

        setError(
            "Unable to preview this PDF. Please check the backend."
        );
    };


    // ============================================================
    // MOUSE DOWN
    // ============================================================

    const handleMouseDown = (event) => {

        if (!previewRef.current) {
            return;
        }

        event.preventDefault();

        const pageElement =
            previewRef.current.querySelector(
                ".react-pdf__Page"
            );

        if (!pageElement) {
            return;
        }

        const rect =
            pageElement.getBoundingClientRect();

        const x =
            event.clientX - rect.left;

        const y =
            event.clientY - rect.top;

        setStartPoint({
            x,
            y
        });

        setSelection({
            x,
            y,
            width: 0,
            height: 0
        });

        setIsSelecting(true);
    };


    // ============================================================
    // MOUSE MOVE
    // ============================================================

    const handleMouseMove = (event) => {

        if (
            !isSelecting ||
            !previewRef.current
        ) {
            return;
        }

        const pageElement =
            previewRef.current.querySelector(
                ".react-pdf__Page"
            );

        if (!pageElement) {
            return;
        }

        const rect =
            pageElement.getBoundingClientRect();

        const currentX =
            Math.max(
                0,
                Math.min(
                    event.clientX - rect.left,
                    rect.width
                )
            );

        const currentY =
            Math.max(
                0,
                Math.min(
                    event.clientY - rect.top,
                    rect.height
                )
            );

        const x =
            Math.min(
                startPoint.x,
                currentX
            );

        const y =
            Math.min(
                startPoint.y,
                currentY
            );

        const width =
            Math.abs(
                currentX - startPoint.x
            );

        const height =
            Math.abs(
                currentY - startPoint.y
            );

        setSelection({
            x,
            y,
            width,
            height
        });
    };


    // ============================================================
    // MOUSE UP
    // ============================================================

    const handleMouseUp = () => {

        setIsSelecting(false);
    };


    // ============================================================
    // CLEAR SELECTION
    // ============================================================

    const clearSelection = () => {

        setSelection({
            x: 0,
            y: 0,
            width: 0,
            height: 0
        });
    };


    // ============================================================
    // PREVIOUS PAGE
    // ============================================================

    const previousPage = () => {

        setPageNumber(
            current =>
                Math.max(
                    1,
                    current - 1
                )
        );

        clearSelection();
    };


    // ============================================================
    // NEXT PAGE
    // ============================================================

    const nextPage = () => {

        setPageNumber(
            current =>
                Math.min(
                    numPages,
                    current + 1
                )
        );

        clearSelection();
    };


    // ============================================================
    // ADD LINK
    // ============================================================

    const handleAddLink = async () => {

        setMessage("");
        setError("");

        if (!selectedPdf) {

            setError(
                "Please select a PDF."
            );

            return;
        }

        if (!url.trim()) {

            setError(
                "Please enter a URL."
            );

            return;
        }

        if (
            !url.startsWith("http://") &&
            !url.startsWith("https://")
        ) {

            setError(
                "URL must start with http:// or https://"
            );

            return;
        }

        if (
            selection.width < 5 ||
            selection.height < 5
        ) {

            setError(
                "Please drag on the PDF to select the link area."
            );

            return;
        }


        try {

            setLoading(true);

            const pageElement =
                previewRef.current?.querySelector(
                    ".react-pdf__Page"
                );

            if (!pageElement) {

                throw new Error(
                    "PDF page is not available."
                );
            }


            const displayedWidth =
                pageElement.getBoundingClientRect()
                    .width;

            const displayedHeight =
                pageElement.getBoundingClientRect()
                    .height;


            if (
                displayedWidth <= 0 ||
                displayedHeight <= 0
            ) {

                throw new Error(
                    "Invalid PDF preview size."
                );
            }


            const canvas =
                pageElement.querySelector(
                    "canvas"
                );


            let pdfPageWidth = 595;
            let pdfPageHeight = 842;


            if (canvas) {

                const scaleX =
                    displayedWidth /
                    canvas.width;

                const scaleY =
                    displayedHeight /
                    canvas.height;

                if (
                    scaleX > 0 &&
                    scaleY > 0
                ) {

                    pdfPageWidth =
                        canvas.width /
                        scaleX;

                    pdfPageHeight =
                        canvas.height /
                        scaleY;
                }
            }


            const scaleX =
                pdfPageWidth /
                displayedWidth;

            const scaleY =
                pdfPageHeight /
                displayedHeight;


            const pdfX =
                selection.x * scaleX;

            const pdfY =
                selection.y * scaleY;

            const pdfWidth =
                selection.width * scaleX;

            const pdfHeight =
                selection.height * scaleY;


            const params =
                new URLSearchParams();

            params.append(
                "pdfId",
                selectedPdf
            );

            params.append(
                "pageNumber",
                pageNumber
            );

            params.append(
                "x",
                pdfX.toFixed(2)
            );

            params.append(
                "y",
                pdfY.toFixed(2)
            );

            params.append(
                "width",
                pdfWidth.toFixed(2)
            );

            params.append(
                "height",
                pdfHeight.toFixed(2)
            );

            params.append(
                "url",
                url.trim()
            );


            if (outputFileName.trim()) {

                params.append(
                    "outputFileName",
                    outputFileName.trim()
                );
            }


            const response =
                await fetch(
                    `${API_BASE_URL}/api/pdfs/add-link?${params.toString()}`,
                    {
                        method: "POST"
                    }
                );


            let data = {};

            try {

                data =
                    await response.json();

            } catch {

                data = {};
            }


            if (!response.ok) {

                throw new Error(
                    data.error ||
                    data.message ||
                    "Failed to add PDF link."
                );
            }


            setMessage(
                data.message ||
                "PDF link added successfully!"
            );


            if (data.downloadUrl) {

                const downloadUrl =
                    data.downloadUrl.startsWith(
                        "http"
                    )
                        ? data.downloadUrl
                        : `${API_BASE_URL}${data.downloadUrl}`;


                const downloadLink =
                    document.createElement(
                        "a"
                    );

                downloadLink.href =
                    downloadUrl;

                downloadLink.download =
                    data.fileName ||
                    "linked.pdf";

                document.body.appendChild(
                    downloadLink
                );

                downloadLink.click();

                document.body.removeChild(
                    downloadLink
                );
            }


            clearSelection();

        } catch (err) {

            console.error(
                "Add link error:",
                err
            );

            setError(
                err.message ||
                "Something went wrong."
            );

        } finally {

            setLoading(false);
        }
    };


    // ============================================================
    // SELECTION STYLE
    // ============================================================

    const selectionStyle = {

        position: "absolute",

        left: `${selection.x}px`,

        top: `${selection.y}px`,

        width: `${selection.width}px`,

        height: `${selection.height}px`,

        border:
            selection.width > 0 &&
            selection.height > 0
                ? "3px solid #f59e0b"
                : "none",

        backgroundColor:
            "rgba(250, 204, 21, 0.25)",

        pointerEvents: "none",

        boxSizing: "border-box",

        zIndex: 100
    };


    // ============================================================
    // RETURN
    // ============================================================

    return (

        <div
            style={{
                maxWidth: "1000px",
                margin: "0 auto",
                padding: "30px",
                color: "#111827",
                boxSizing: "border-box"
            }}
        >

            {/* HEADER */}

            <div
                style={{
                    textAlign: "center",
                    marginBottom: "30px"
                }}
            >

                <h1
                    style={{
                        margin: "0 0 10px",
                        color: "#111827"
                    }}
                >
                    🔗 Add PDF Links
                </h1>

                <p
                    style={{
                        margin: 0,
                        color: "#6b7280"
                    }}
                >
                    Select an area on your PDF
                    and make it clickable.
                </p>

            </div>


            {/* PDF SELECT */}

            <div
                style={{
                    marginBottom: "20px"
                }}
            >

                <label
                    style={{
                        display: "block",
                        fontWeight: "600",
                        marginBottom: "8px",
                        color: "#111827"
                    }}
                >
                    Select PDF
                </label>

                <select
                    value={selectedPdf}
                    onChange={event =>
                        setSelectedPdf(
                            event.target.value
                        )
                    }
                    style={{
                        width: "100%",
                        padding: "12px",
                        borderRadius: "8px",
                        border:
                            "1px solid #cbd5e1",
                        backgroundColor:
                            "#ffffff",
                        color: "#111827",
                        fontSize: "15px",
                        boxSizing: "border-box"
                    }}
                >

                    <option value="">
                        -- Select a PDF --
                    </option>

                    {pdfList.map(
                        (pdf, index) => (

                            <option
                                key={
                                    getPdfId(pdf) ||
                                    index
                                }
                                value={
                                    getPdfId(pdf)
                                }
                            >
                                {getPdfName(pdf)}
                            </option>

                        )
                    )}

                </select>

            </div>


            {/* PAGE CONTROLS */}

            {selectedPdf && (

                <div
                    style={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        gap: "15px",
                        marginBottom: "20px"
                    }}
                >

                    <button
                        type="button"
                        onClick={
                            previousPage
                        }
                        disabled={
                            pageNumber <= 1
                        }
                        style={{
                            padding:
                                "10px 18px",
                            borderRadius: "7px",
                            border:
                                "1px solid #cbd5e1",
                            backgroundColor:
                                pageNumber <= 1
                                    ? "#e5e7eb"
                                    : "#ffffff",
                            color: "#111827",
                            cursor:
                                pageNumber <= 1
                                    ? "not-allowed"
                                    : "pointer"
                        }}
                    >
                        ← Previous
                    </button>


                    <span
                        style={{
                            fontWeight: "600",
                            color: "#111827"
                        }}
                    >
                        Page {pageNumber}
                        {numPages
                            ? ` / ${numPages}`
                            : ""}
                    </span>


                    <button
                        type="button"
                        onClick={
                            nextPage
                        }
                        disabled={
                            !numPages ||
                            pageNumber >=
                            numPages
                        }
                        style={{
                            padding:
                                "10px 18px",
                            borderRadius: "7px",
                            border:
                                "1px solid #cbd5e1",
                            backgroundColor:
                                !numPages ||
                                pageNumber >=
                                numPages
                                    ? "#e5e7eb"
                                    : "#ffffff",
                            color: "#111827",
                            cursor:
                                !numPages ||
                                pageNumber >=
                                numPages
                                    ? "not-allowed"
                                    : "pointer"
                        }}
                    >
                        Next →
                    </button>

                </div>

            )}


            {/* PDF PREVIEW */}

            {pdfUrl && (

                <div
                    style={{
                        marginBottom: "25px"
                    }}
                >

                    <div
                        style={{
                            textAlign: "center",
                            marginBottom: "10px",
                            color: "#374151",
                            fontWeight: "600"
                        }}
                    >
                        🖱️ Drag on the PDF
                        to select the clickable area
                    </div>


                    <div
                        ref={previewRef}
                        onMouseDown={
                            handleMouseDown
                        }
                        onMouseMove={
                            handleMouseMove
                        }
                        onMouseUp={
                            handleMouseUp
                        }
                        onMouseLeave={
                            handleMouseUp
                        }
                        style={{
                            position: "relative",
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "flex-start",
                            width: "100%",
                            minHeight: "300px",
                            overflow: "auto",
                            backgroundColor:
                                "#f3f4f6",
                            border:
                                "1px solid #d1d5db",
                            borderRadius: "10px",
                            padding: "20px",
                            boxSizing:
                                "border-box",
                            cursor: "crosshair",
                            userSelect: "none"
                        }}
                    >

                        <div
                            style={{
                                position: "relative",
                                display: "inline-block",
                                boxShadow:
                                    "0 5px 20px rgba(0,0,0,0.15)"
                            }}
                        >

                            <Document
                                file={pdfUrl}
                                onLoadSuccess={
                                    onDocumentLoadSuccess
                                }
                                onLoadError={
                                    onDocumentLoadError
                                }
                                loading={
                                    <div
                                        style={{
                                            padding:
                                                "50px",
                                            color:
                                                "#374151"
                                        }}
                                    >
                                        Loading PDF...
                                    </div>
                                }
                                error={
                                    <div
                                        style={{
                                            padding:
                                                "50px",
                                            color:
                                                "#dc2626"
                                        }}
                                    >
                                        Unable to load PDF preview.
                                    </div>
                                }
                            >

                                <Page
                                    pageNumber={
                                        pageNumber
                                    }
                                    width={700}
                                    renderTextLayer={
                                        true
                                    }
                                    renderAnnotationLayer={
                                        true
                                    }
                                />

                            </Document>


                            {/* ================================================= */}
                            {/* VISIBLE LINK SELECTION */}
                            {/* ================================================= */}

                            <div
                                style={
                                    selectionStyle
                                }
                            >

                                {selection.width >
                                    20 &&
                                    selection.height >
                                    20 && (

                                        <span
                                            style={{
                                                position:
                                                    "absolute",

                                                left:
                                                    "50%",

                                                top:
                                                    "50%",

                                                transform:
                                                    "translate(-50%, -50%)",

                                                backgroundColor:
                                                    "#facc15",

                                                color:
                                                    "#111827",

                                                padding:
                                                    "6px 12px",

                                                borderRadius:
                                                    "5px",

                                                fontSize:
                                                    "12px",

                                                fontWeight:
                                                    "700",

                                                whiteSpace:
                                                    "nowrap",

                                                border:
                                                    "1px solid #ca8a04",

                                                boxShadow:
                                                    "0 2px 6px rgba(0,0,0,0.25)"
                                            }}
                                        >
                                            🔗 Link Area
                                        </span>

                                    )}

                            </div>

                        </div>

                    </div>


                    {/* SELECTION INFO */}

                    {selection.width >
                        0 &&
                        selection.height >
                        0 && (

                            <div
                                style={{
                                    marginTop: "12px",
                                    padding: "10px 14px",
                                    backgroundColor:
                                        "#fffbeb",
                                    border:
                                        "1px solid #fcd34d",
                                    borderRadius: "7px",
                                    color: "#78350f",
                                    fontSize: "14px"
                                }}
                            >

                                <strong>
                                    🔗 Selected Link Area:
                                </strong>{" "}

                                {Math.round(
                                    selection.width
                                )}
                                ×
                                {Math.round(
                                    selection.height
                                )} px


                                <button
                                    type="button"
                                    onClick={
                                        clearSelection
                                    }
                                    style={{
                                        marginLeft:
                                            "15px",
                                        border: "none",
                                        background:
                                            "transparent",
                                        color:
                                            "#b45309",
                                        cursor:
                                            "pointer",
                                        fontWeight:
                                            "700"
                                    }}
                                >
                                    Clear
                                </button>

                            </div>

                        )}

                </div>

            )}


            {/* URL */}

            <div
                style={{
                    marginBottom: "20px"
                }}
            >

                <label
                    style={{
                        display: "block",
                        fontWeight: "600",
                        marginBottom: "8px",
                        color: "#111827"
                    }}
                >
                    🔗 Website URL
                </label>

                <input
                    type="url"
                    value={url}
                    onChange={event =>
                        setUrl(
                            event.target.value
                        )
                    }
                    placeholder="https://example.com"
                    style={{
                        width: "100%",
                        padding: "12px 14px",
                        borderRadius: "8px",
                        border:
                            "1px solid #cbd5e1",
                        backgroundColor:
                            "#ffffff",
                        color: "#111827",
                        fontSize: "15px",
                        boxSizing: "border-box"
                    }}
                />

            </div>


            {/* OUTPUT NAME */}

            <div
                style={{
                    marginBottom: "20px"
                }}
            >

                <label
                    style={{
                        display: "block",
                        fontWeight: "600",
                        marginBottom: "8px",
                        color: "#111827"
                    }}
                >
                    Output File Name
                </label>

                <input
                    type="text"
                    value={outputFileName}
                    onChange={event =>
                        setOutputFileName(
                            event.target.value
                        )
                    }
                    placeholder="linked.pdf"
                    style={{
                        width: "100%",
                        padding: "12px 14px",
                        borderRadius: "8px",
                        border:
                            "1px solid #cbd5e1",
                        backgroundColor:
                            "#ffffff",
                        color: "#111827",
                        fontSize: "15px",
                        boxSizing: "border-box"
                    }}
                />

            </div>


            {/* ERROR */}

            {error && (

                <div
                    style={{
                        marginBottom: "20px",
                        padding: "13px",
                        borderRadius: "8px",
                        backgroundColor:
                            "#fee2e2",
                        border:
                            "1px solid #fecaca",
                        color: "#b91c1c"
                    }}
                >
                    ❌ {error}
                </div>

            )}


            {/* SUCCESS */}

            {message && (

                <div
                    style={{
                        marginBottom: "20px",
                        padding: "13px",
                        borderRadius: "8px",
                        backgroundColor:
                            "#dcfce7",
                        border:
                            "1px solid #bbf7d0",
                        color: "#166534"
                    }}
                >
                    ✅ {message}
                </div>

            )}


            {/* ADD LINK */}

            <button
                type="button"
                onClick={
                    handleAddLink
                }
                disabled={
                    loading ||
                    !selectedPdf
                }
                style={{
                    width: "100%",
                    padding: "16px",
                    border: "none",
                    borderRadius: "8px",
                    backgroundColor:
                        loading ||
                        !selectedPdf
                            ? "#9ca3af"
                            : "#2563eb",
                    color: "#ffffff",
                    fontSize: "17px",
                    fontWeight: "700",
                    cursor:
                        loading ||
                        !selectedPdf
                            ? "not-allowed"
                            : "pointer",
                    boxShadow:
                        "0 4px 10px rgba(37,99,235,0.25)"
                }}
            >
                {loading
                    ? "⏳ Adding Link..."
                    : "🔗 Add Link to PDF"}
            </button>

        </div>
    );
}

export default PdfLinks;
import React, {
    useEffect,
    useRef,
    useState
} from "react";

import {
    Document,
    Page,
    pdfjs
} from "react-pdf";

// ============================================================
// PDF.JS WORKER
// ============================================================

pdfjs.GlobalWorkerOptions.workerSrc =
    `https://unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;


const API_BASE_URL = "http://localhost:8080";


// ============================================================
// COMPONENT
// ============================================================

function PdfCrop({
                     pdfList = [],
                     onSuccess,
                     onBackHome
                 }) {

    // ========================================================
    // STATE
    // ========================================================

    const [selectedPdf, setSelectedPdf] =
        useState("");

    const [numPages, setNumPages] =
        useState(0);

    const [pageNumber, setPageNumber] =
        useState(1);

    const [pdfUrl, setPdfUrl] =
        useState("");

    const [loadingPdf, setLoadingPdf] =
        useState(false);

    const [cropping, setCropping] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [resultFileName, setResultFileName] =
        useState("");


    // ========================================================
    // PDF DISPLAY SIZE
    // ========================================================

    const [pageSize, setPageSize] =
        useState({
            width: 0,
            height: 0
        });


    // ========================================================
    // CROP BOX
    //
    // Values are percentages of displayed PDF page.
    // This makes the crop work regardless of screen size.
    // ========================================================

    const [cropBox, setCropBox] =
        useState(null);


    // ========================================================
    // INTERACTION
    // ========================================================

    const [dragging, setDragging] =
        useState(false);

    const [resizing, setResizing] =
        useState(null);

    const [dragStart, setDragStart] =
        useState(null);


    // ========================================================
    // REFS
    // ========================================================

    const pageContainerRef =
        useRef(null);


    // ========================================================
    // LOAD SELECTED PDF
    // ========================================================

    useEffect(() => {

        if (!selectedPdf) {

            setPdfUrl("");
            setNumPages(0);
            setPageNumber(1);
            setCropBox(null);

            return;
        }


        setPdfUrl(
            `${API_BASE_URL}/api/pdfs/download/${selectedPdf}`
        );

        setPageNumber(1);
        setCropBox(null);

        setMessage("");
        setError("");

    }, [selectedPdf]);


    // ========================================================
    // PDF LOADED
    // ========================================================

    const handleDocumentLoadSuccess = ({
                                           numPages
                                       }) => {

        setNumPages(numPages);

        setPageNumber(1);

    };


    // ========================================================
    // PAGE RENDER SUCCESS
    // ========================================================

    const handlePageLoadSuccess = (
        page
    ) => {

        const viewport =
            page.getViewport({
                scale: 1
            });

        setPageSize({
            width: viewport.width,
            height: viewport.height
        });
    };


    // ========================================================
    // PAGE SIZE
    // ========================================================

    const displayPageWidth =
        Math.min(
            850,
            pageSize.width || 850
        );


    const displayPageHeight =
        pageSize.width > 0
            ? (
            pageSize.height /
            pageSize.width
        ) * displayPageWidth
            : 1100;


    // ========================================================
    // GET MOUSE POSITION
    // ========================================================

    const getRelativePosition = (
        event
    ) => {

        if (!pageContainerRef.current) {
            return null;
        }


        const rect =
            pageContainerRef.current.getBoundingClientRect();


        let x =
            event.clientX -
            rect.left;

        let y =
            event.clientY -
            rect.top;


        x =
            Math.max(
                0,
                Math.min(
                    x,
                    rect.width
                )
            );


        y =
            Math.max(
                0,
                Math.min(
                    y,
                    rect.height
                )
            );


        return {
            x,
            y,
            width: rect.width,
            height: rect.height
        };
    };


    // ========================================================
    // START NEW CROP
    // ========================================================

    const handlePageMouseDown = (
        event
    ) => {

        // Ignore if clicking existing crop box
        if (
            event.target.closest(
                ".cropSelection"
            )
        ) {
            return;
        }


        const position =
            getRelativePosition(event);


        if (!position) {
            return;
        }


        setCropBox({
            left: position.x,
            top: position.y,
            width: 0,
            height: 0
        });


        setDragging(true);

        setDragStart({
            x: position.x,
            y: position.y
        });


        setMessage("");
        setError("");

    };


    // ========================================================
    // MOUSE MOVE
    // ========================================================

    const handleMouseMove = (
        event
    ) => {

        const position =
            getRelativePosition(event);


        if (!position) {
            return;
        }


        // ----------------------------------------------------
        // CREATE NEW SELECTION
        // ----------------------------------------------------

        if (
            dragging &&
            dragStart
        ) {

            const left =
                Math.min(
                    dragStart.x,
                    position.x
                );

            const top =
                Math.min(
                    dragStart.y,
                    position.y
                );

            const width =
                Math.abs(
                    position.x -
                    dragStart.x
                );

            const height =
                Math.abs(
                    position.y -
                    dragStart.y
                );


            setCropBox({
                left,
                top,
                width,
                height
            });

            return;
        }


        // ----------------------------------------------------
        // RESIZE EXISTING BOX
        // ----------------------------------------------------

        if (
            resizing &&
            cropBox
        ) {

            resizeCropBox(
                position.x,
                position.y
            );

            return;
        }


        // ----------------------------------------------------
        // MOVE EXISTING BOX
        // ----------------------------------------------------

        if (
            cropBox &&
            dragging &&
            dragStart
        ) {

            const dx =
                position.x -
                dragStart.x;

            const dy =
                position.y -
                dragStart.y;


            let newLeft =
                cropBox.left + dx;

            let newTop =
                cropBox.top + dy;


            newLeft =
                Math.max(
                    0,
                    Math.min(
                        newLeft,
                        position.width -
                        cropBox.width
                    )
                );


            newTop =
                Math.max(
                    0,
                    Math.min(
                        newTop,
                        position.height -
                        cropBox.height
                    )
                );


            setCropBox({
                ...cropBox,
                left: newLeft,
                top: newTop
            });


            setDragStart({
                x: position.x,
                y: position.y
            });
        }
    };


    // ========================================================
    // RESIZE CROP BOX
    // ========================================================

    const resizeCropBox = (
        mouseX,
        mouseY
    ) => {

        if (
            !cropBox ||
            !pageContainerRef.current
        ) {
            return;
        }


        const container =
            pageContainerRef.current
                .getBoundingClientRect();


        let left =
            cropBox.left;

        let top =
            cropBox.top;

        let right =
            cropBox.left +
            cropBox.width;

        let bottom =
            cropBox.top +
            cropBox.height;


        const minSize = 20;


        if (
            resizing === "top-left"
        ) {

            left =
                Math.min(
                    mouseX,
                    right - minSize
                );

            top =
                Math.min(
                    mouseY,
                    bottom - minSize
                );

        }


        else if (
            resizing === "top-right"
        ) {

            right =
                Math.max(
                    mouseX,
                    left + minSize
                );

            top =
                Math.min(
                    mouseY,
                    bottom - minSize
                );

        }


        else if (
            resizing === "bottom-left"
        ) {

            left =
                Math.min(
                    mouseX,
                    right - minSize
                );

            bottom =
                Math.max(
                    mouseY,
                    top + minSize
                );

        }


        else if (
            resizing === "bottom-right"
        ) {

            right =
                Math.max(
                    mouseX,
                    left + minSize
                );

            bottom =
                Math.max(
                    mouseY,
                    top + minSize
                );
        }


        // Keep inside page

        left =
            Math.max(
                0,
                left
            );

        top =
            Math.max(
                0,
                top
            );

        right =
            Math.min(
                container.width,
                right
            );

        bottom =
            Math.min(
                container.height,
                bottom
            );


        setCropBox({
            left,
            top,
            width:
                right - left,
            height:
                bottom - top
        });
    };


    // ========================================================
    // START RESIZE
    // ========================================================

    const handleResizeStart = (
        event,
        direction
    ) => {

        event.stopPropagation();

        setResizing(direction);

        setDragging(false);

    };


    // ========================================================
    // START MOVE
    // ========================================================

    const handleCropMouseDown = (
        event
    ) => {

        event.stopPropagation();


        const position =
            getRelativePosition(event);


        if (!position || !cropBox) {
            return;
        }


        setDragging(true);

        setResizing(null);


        setDragStart({
            x: position.x,
            y: position.y
        });
    };


    // ========================================================
    // MOUSE UP
    // ========================================================

    const handleMouseUp = () => {

        setDragging(false);

        setResizing(null);

        setDragStart(null);

    };


    // ========================================================
    // CLEAR CROP
    // ========================================================

    const handleClearCrop = () => {

        setCropBox(null);

        setMessage("");

        setError("");

    };


    // ========================================================
    // CROP PDF
    // ========================================================

    const handleCropPdf = async () => {

        setMessage("");
        setError("");
        setResultFileName("");


        if (!selectedPdf) {

            setError(
                "Please select a PDF"
            );

            return;
        }


        if (!cropBox) {

            setError(
                "Please select an area to crop"
            );

            return;
        }


        if (
            cropBox.width < 10 ||
            cropBox.height < 10
        ) {

            setError(
                "Please select a larger area"
            );

            return;
        }


        if (
            !pageSize.width ||
            !pageSize.height
        ) {

            setError(
                "PDF page size could not be detected"
            );

            return;
        }


        setCropping(true);


        try {

            // =================================================
            // DISPLAY → PDF COORDINATES
            // =================================================

            const scaleX =
                pageSize.width /
                displayPageWidth;


            const scaleY =
                pageSize.height /
                displayPageHeight;


            const pdfX =
                cropBox.left *
                scaleX;


            // Browser uses TOP → DOWN.
            // PDFBox uses BOTTOM → UP.

            const pdfYFromTop =
                cropBox.top *
                scaleY;


            const pdfWidth =
                cropBox.width *
                scaleX;


            const pdfHeight =
                cropBox.height *
                scaleY;


            const pdfY =
                pageSize.height -
                pdfYFromTop -
                pdfHeight;


            // =================================================
            // OUTPUT FILE
            // =================================================

            let outputFileName =
                `cropped-page-${pageNumber}.pdf`;


            // =================================================
            // REQUEST
            // =================================================

            const params =
                new URLSearchParams();


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
                "outputFileName",
                outputFileName
            );


            const url =
                `${API_BASE_URL}/api/pdfs/crop/${selectedPdf}?${params.toString()}`;


            console.log(
                "Crop request:",
                {
                    pageNumber,
                    x: pdfX,
                    y: pdfY,
                    width: pdfWidth,
                    height: pdfHeight
                }
            );


            // =================================================
            // SEND REQUEST
            // =================================================

            const response =
                await fetch(
                    url,
                    {
                        method: "POST"
                    }
                );


            const text =
                await response.text();


            let result = null;


            try {

                result =
                    JSON.parse(text);

            } catch {

                result = null;

            }


            if (!response.ok) {

                throw new Error(
                    result?.message ||
                    result?.error ||
                    text ||
                    "PDF cropping failed"
                );
            }


            // =================================================
            // SUCCESS
            // =================================================

            const generatedFileName =
                result?.fileName ||
                outputFileName;


            setResultFileName(
                generatedFileName
            );


            setMessage(
                result?.message ||
                "PDF cropped successfully!"
            );


            if (onSuccess) {
                onSuccess();
            }

        } catch (err) {

            console.error(
                "Crop PDF error:",
                err
            );


            setError(
                err.message ||
                "PDF cropping failed"
            );

        } finally {

            setCropping(false);
        }
    };


    // ========================================================
    // DOWNLOAD
    // ========================================================

    const handleDownload = () => {

        if (!resultFileName) {
            return;
        }


        const downloadUrl =
            `${API_BASE_URL}/api/pdfs/download-file/${encodeURIComponent(
                resultFileName
            )}`;


        const link =
            document.createElement("a");


        link.href =
            downloadUrl;


        link.download =
            resultFileName;


        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);
    };


    // ========================================================
    // BACK HOME
    // ========================================================

    const handleBackHome = () => {

        if (onBackHome) {
            onBackHome();
        }
    };


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div
            className="featurePage"
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
        >

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="featureHeader">

                <h1>
                    ✂️ Crop PDF
                </h1>

                <p>
                    Select the area you want to keep.
                    Drag and resize the crop box.
                </p>

            </div>


            {/* =================================================
                PDF SELECT
            ================================================= */}

            <div className="featureCard">

                <div className="formGroup">

                    <label>
                        Select PDF
                    </label>

                    <select
                        value={selectedPdf}
                        onChange={(e) =>
                            setSelectedPdf(
                                e.target.value
                            )
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


                {/* =================================================
                    PAGE CONTROLS
                ================================================= */}

                {pdfUrl && (

                    <div
                        style={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent:
                                "space-between",
                            gap: "15px",
                            marginBottom: "20px",
                            flexWrap: "wrap"
                        }}
                    >

                        <div>

                            <strong>
                                Page:
                            </strong>

                            {" "}

                            {pageNumber}

                            {" / "}

                            {numPages}

                        </div>


                        <div
                            style={{
                                display: "flex",
                                gap: "10px"
                            }}
                        >

                            <button
                                className="primaryButton"
                                disabled={
                                    pageNumber <= 1
                                }
                                onClick={() => {

                                    setPageNumber(
                                        pageNumber - 1
                                    );

                                    setCropBox(null);

                                }}
                            >
                                ← Previous
                            </button>


                            <button
                                className="primaryButton"
                                disabled={
                                    pageNumber >=
                                    numPages
                                }
                                onClick={() => {

                                    setPageNumber(
                                        pageNumber + 1
                                    );

                                    setCropBox(null);

                                }}
                            >
                                Next →
                            </button>

                        </div>

                    </div>

                )}


                {/* =================================================
                    INSTRUCTIONS
                ================================================= */}

                {pdfUrl && (

                    <div
                        style={{
                            padding: "12px 16px",
                            marginBottom: "18px",
                            borderRadius: "10px",
                            background:
                                "rgba(59,130,246,0.08)"
                        }}
                    >

                        🖱️ <strong>
                        Drag
                    </strong>{" "}
                        on the PDF to select an area.

                        <br />

                        ↔️ Drag the selected box to move it.

                        <br />

                        ⤢ Drag the corners to resize it.

                    </div>

                )}


                {/* =================================================
                    PDF VIEWER
                ================================================= */}

                {pdfUrl && (

                    <div
                        style={{
                            width: "100%",
                            overflowX: "auto",
                            padding: "20px 0"
                        }}
                    >

                        <div
                            style={{
                                display: "flex",
                                justifyContent:
                                    "center",
                                minWidth:
                                displayPageWidth
                            }}
                        >

                            <div
                                ref={
                                    pageContainerRef
                                }
                                style={{
                                    position:
                                        "relative",
                                    width:
                                    displayPageWidth,
                                    height:
                                    displayPageHeight,
                                    cursor:
                                        cropBox
                                            ? "default"
                                            : "crosshair",
                                    userSelect:
                                        "none"
                                }}
                                onMouseDown={
                                    handlePageMouseDown
                                }
                            >

                                <Document
                                    file={pdfUrl}
                                    onLoadSuccess={
                                        handleDocumentLoadSuccess
                                    }
                                    loading={
                                        <div
                                            style={{
                                                padding:
                                                    "50px",
                                                textAlign:
                                                    "center"
                                            }}
                                        >
                                            ⏳ Loading PDF...
                                        </div>
                                    }
                                    error={
                                        <div
                                            className="errorMessage"
                                        >
                                            ❌ Unable to load
                                            PDF.
                                        </div>
                                    }
                                >

                                    <Page
                                        pageNumber={pageNumber}
                                        width={displayPageWidth}
                                        renderTextLayer={false}
                                        renderAnnotationLayer={false}
                                        onLoadSuccess={handlePageLoadSuccess}
                                    />

                                </Document>


                                {/* =================================================
                                    DARK OVERLAY
                                ================================================= */}

                                {cropBox && (

                                    <>
                                        <div
                                            style={{
                                                position:
                                                    "absolute",
                                                inset: 0,
                                                background:
                                                    "rgba(0,0,0,0.45)",
                                                pointerEvents:
                                                    "none",
                                                clipPath:
                                                    `polygon(
                                                        0% 0%,
                                                        100% 0%,
                                                        100% 100%,
                                                        0% 100%,
                                                        0% 0%,
                                                        ${(cropBox.left / displayPageWidth) * 100}% ${(cropBox.top / displayPageHeight) * 100}%,
                                                        ${(cropBox.left / displayPageWidth) * 100}% ${((cropBox.top + cropBox.height) / displayPageHeight) * 100}%,
                                                        ${((cropBox.left + cropBox.width) / displayPageWidth) * 100}% ${((cropBox.top + cropBox.height) / displayPageHeight) * 100}%,
                                                        ${((cropBox.left + cropBox.width) / displayPageWidth) * 100}% ${(cropBox.top / displayPageHeight) * 100}%
                                                    )`
                                            }}
                                        />
                                    </>

                                )}


                                {/* =================================================
                                    CROP SELECTION
                                ================================================= */}

                                {cropBox && (

                                    <div
                                        className="cropSelection"
                                        onMouseDown={
                                            handleCropMouseDown
                                        }
                                        style={{
                                            position:
                                                "absolute",
                                            left:
                                            cropBox.left,
                                            top:
                                            cropBox.top,
                                            width:
                                            cropBox.width,
                                            height:
                                            cropBox.height,
                                            border:
                                                "2px solid #2563eb",
                                            boxSizing:
                                                "border-box",
                                            cursor:
                                                "move",
                                            background:
                                                "transparent"
                                        }}
                                    >

                                        {/* Top Left */}

                                        <span
                                            onMouseDown={
                                                (e) =>
                                                    handleResizeStart(
                                                        e,
                                                        "top-left"
                                                    )
                                            }
                                            style={{
                                                position:
                                                    "absolute",
                                                width:
                                                    "14px",
                                                height:
                                                    "14px",
                                                left:
                                                    "-7px",
                                                top:
                                                    "-7px",
                                                background:
                                                    "#ffffff",
                                                border:
                                                    "2px solid #2563eb",
                                                borderRadius:
                                                    "50%",
                                                cursor:
                                                    "nwse-resize"
                                            }}
                                        />


                                        {/* Top Right */}

                                        <span
                                            onMouseDown={
                                                (e) =>
                                                    handleResizeStart(
                                                        e,
                                                        "top-right"
                                                    )
                                            }
                                            style={{
                                                position:
                                                    "absolute",
                                                width:
                                                    "14px",
                                                height:
                                                    "14px",
                                                right:
                                                    "-7px",
                                                top:
                                                    "-7px",
                                                background:
                                                    "#ffffff",
                                                border:
                                                    "2px solid #2563eb",
                                                borderRadius:
                                                    "50%",
                                                cursor:
                                                    "nesw-resize"
                                            }}
                                        />


                                        {/* Bottom Left */}

                                        <span
                                            onMouseDown={
                                                (e) =>
                                                    handleResizeStart(
                                                        e,
                                                        "bottom-left"
                                                    )
                                            }
                                            style={{
                                                position:
                                                    "absolute",
                                                width:
                                                    "14px",
                                                height:
                                                    "14px",
                                                left:
                                                    "-7px",
                                                bottom:
                                                    "-7px",
                                                background:
                                                    "#ffffff",
                                                border:
                                                    "2px solid #2563eb",
                                                borderRadius:
                                                    "50%",
                                                cursor:
                                                    "nesw-resize"
                                            }}
                                        />


                                        {/* Bottom Right */}

                                        <span
                                            onMouseDown={
                                                (e) =>
                                                    handleResizeStart(
                                                        e,
                                                        "bottom-right"
                                                    )
                                            }
                                            style={{
                                                position:
                                                    "absolute",
                                                width:
                                                    "14px",
                                                height:
                                                    "14px",
                                                right:
                                                    "-7px",
                                                bottom:
                                                    "-7px",
                                                background:
                                                    "#ffffff",
                                                border:
                                                    "2px solid #2563eb",
                                                borderRadius:
                                                    "50%",
                                                cursor:
                                                    "nwse-resize"
                                            }}
                                        />

                                    </div>

                                )}

                            </div>

                        </div>

                    </div>

                )}


                {/* =================================================
                    MESSAGES
                ================================================= */}

                {error && (

                    <div className="errorMessage">

                        ❌ {error}

                    </div>

                )}


                {message && (

                    <div className="successMessage">

                        ✅ {message}

                    </div>

                )}


                {/* =================================================
                    ACTION BUTTONS
                ================================================= */}

                {pdfUrl && (

                    <div
                        style={{
                            display: "flex",
                            gap: "12px",
                            marginTop: "20px",
                            flexWrap: "wrap"
                        }}
                    >

                        <button
                            className="primaryButton"
                            onClick={
                                handleCropPdf
                            }
                            disabled={
                                cropping ||
                                !cropBox
                            }
                        >

                            {cropping
                                ? "⏳ Cropping..."
                                : "✂️ Crop PDF"}

                        </button>


                        <button
                            className="primaryButton"
                            onClick={
                                handleClearCrop
                            }
                            disabled={
                                cropping ||
                                !cropBox
                            }
                        >

                            🔄 Clear Selection

                        </button>

                    </div>

                )}


                {/* =================================================
                    DOWNLOAD
                ================================================= */}

                {resultFileName && (

                    <div
                        className="resultActions"
                        style={{
                            marginTop: "20px"
                        }}
                    >

                        <button
                            className="downloadButton"
                            onClick={
                                handleDownload
                            }
                        >

                            ⬇️ Download Cropped PDF

                        </button>


                        <p className="resultFileName">

                            📄 {resultFileName}

                        </p>

                    </div>

                )}

            </div>

        </div>
    );
}


export default PdfCrop;
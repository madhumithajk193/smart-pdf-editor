import { useEffect, useState } from "react";
import "./App.css";

import Header from "./components/Header";
import Upload from "./components/Upload";
import Merge from "./components/Merge";
import Split from "./components/Split";
import PdfTable from "./components/PdfTable";
import DeletePdf from "./components/DeletePdf";
import RotatePdf from "./components/RotatePdf";
import ExtractPages from "./components/ExtractPages";
import Watermark from "./components/Watermark";
import PreviewPdf from "./components/PreviewPdf";
import PdfToWord from "./components/PdfToWord.jsx";
import EditPdf from "./components/EditPdf";
import Signature from "./components/Signature";
import Metadata from "./components/Metadata";
import Compression from "./components/Compression";
import PdfToImage from "./components/PdfToImage";
import ImageToPdf from "./components/ImageToPdf";
import Permissions from "./components/Permissions";
import Highlight from "./components/Highlight";
import ReorderPages from "./components/ReorderPages.jsx";
import PageNumbers from "./components/PageNumbers.jsx";
import ProtectPdf from "./components/ProtectPdf";
import WordToPdf from "./components/WordToPdf";
import ExcelToPdf from "./components/ExcelToPdf.jsx";
import PdfToExcel from "./components/PdfToExcel";
import SearchPdf from "./components/SearchPdf.jsx";
import OcrPdf from "./components/OcrPdf.jsx";
import FindReplace from "./components/FindReplace";
import PdfFormField from "./components/PdfFormField";
import PdfBookmark from "./components/PdfBookmark";
import PdfResize from "./components/PdfResize";
import PdfCrop from "./components/PdfCrop";
import PdfLinks from "./components/PdfLinks";
import PdfSignatureVerification from "./components/PdfSignatureVerification";
import PdfProperties from "./components/PdfProperties";
import PdfPrint from "./components/PdfPrint";


function App() {

    const [pdfList, setPdfList] = useState([]);

    const [currentPage, setCurrentPage] =
        useState("dashboard");

    // ============================================================
    // MAIN PAGE TOOL SEARCH
    // ============================================================

    const [toolSearch, setToolSearch] =
        useState("");


    // ============================================================
    // LOAD PDF LIST
    // ============================================================

    const loadPdfs = async () => {

        try {

            const response = await fetch(
                "http://localhost:8080/api/pdfs"
            );

            if (response.ok) {

                const data =
                    await response.json();

                setPdfList(data);
            }

        } catch (error) {

            console.error(
                "Error loading PDFs:",
                error
            );

        }

    };


    useEffect(() => {

        loadPdfs();

    }, []);


    // ============================================================
    // HOME
    // ============================================================

    const goHome = () => {

        setCurrentPage("dashboard");

        setToolSearch("");

        loadPdfs();

    };


    // ============================================================
    // 35 PDF TOOLS
    // ============================================================

    const featureCards = [

        {
            id: "upload",
            icon: "📤",
            title: "Upload PDF",
            description: "Upload your PDF documents",
            color: "blue"
        },

        {
            id: "merge",
            icon: "🔗",
            title: "Merge PDFs",
            description: "Combine multiple PDF files",
            color: "purple"
        },

        {
            id: "split",
            icon: "✂️",
            title: "Split PDF",
            description: "Split pages from a PDF",
            color: "orange"
        },

        {
            id: "extract",
            icon: "📄",
            title: "Extract Pages",
            description: "Extract selected pages",
            color: "green"
        },

        {
            id: "rotate",
            icon: "🔄",
            title: "Rotate PDF",
            description: "Rotate selected pages",
            color: "cyan"
        },

        {
            id: "watermark",
            icon: "💧",
            title: "Watermark PDF",
            description: "Add text watermark",
            color: "pink"
        },

        {
            id: "preview",
            icon: "👁️",
            title: "Preview PDF",
            description: "View your PDF document",
            color: "indigo"
        },

        {
            id: "pdfs",
            icon: "🗂️",
            title: "My PDFs",
            description: "Manage your PDF files",
            color: "yellow"
        },

        {
            id: "pdfBookmark",
            icon: "🔖",
            title: "PDF Bookmarks",
            description: "Add bookmarks to PDF pages",
            color: "orange"
        },

        {
            id: "delete",
            icon: "🗑️",
            title: "Delete PDF",
            description: "Remove unwanted PDFs",
            color: "red"
        },

        {
            id: "pdfLinks",
            icon: "🔗",
            title: "Add PDF Links",
            description: "Add clickable links to PDF",
            color: "blue"
        },

        {
            id: "pdfSignatureVerification",
            icon: "🔐",
            title: "Digital Signature Verification",
            description: "Verify digital signatures in PDF",
            color: "blue"
        },

        {
            id: "findreplace",
            icon: "🔍",
            title: "Find & Replace",
            description: "Find and replace text in your PDF",
            color: "yellow"
        },

        {
            id: "edit",
            icon: "✏️",
            title: "Edit PDF",
            description: "Add text to your PDF",
            color: "blue"
        },

        {
            id: "metadata",
            icon: "📝",
            title: "Edit Metadata",
            description: "Edit PDF information",
            color: "blue"
        },

        {
            id: "pdfCrop",
            icon: "✂️",
            title: "Crop PDF",
            description: "Crop a selected area of a PDF page",
            color: "orange"
        },

        {
            id: "compression",
            icon: "🗜️",
            title: "Compress PDF",
            description: "Reduce PDF file size",
            color: "cyan"
        },

        {
            id: "pdfToImage",
            icon: "🖼️",
            title: "PDF to Images",
            description: "Convert PDF pages to images",
            color: "green"
        },

        {
            id: "imageToPdf",
            icon: "🖼️",
            title: "Images to PDF",
            description: "Convert images into a PDF",
            color: "purple"
        },

        {
            id: "pdfResize",
            icon: "📐",
            title: "Resize PDF Pages",
            description: "Resize PDF pages to A3, A4, A5, Letter or Legal",
            color: "blue"
        },

        {
            id: "permissions",
            icon: "🔒",
            title: "PDF Permissions",
            description: "Control PDF access permissions",
            color: "cyan"
        },

        {
            id: "highlight",
            icon: "🖍️",
            title: "Highlight PDF",
            description: "Highlight words in your PDF",
            color: "yellow"
        },

        {
            id: "reorder",
            icon: "🔢",
            title: "Reorder Pages",
            description: "Change the order of PDF pages",
            color: "orange"
        },

        {
            id: "pageNumbers",
            icon: "🔢",
            title: "Page Numbers",
            description: "Add numbers to PDF pages",
            color: "blue"
        },

        {
            id: "pdfToWord",
            icon: "📝",
            title: "PDF to Word",
            description: "Convert PDF to editable Word",
            color: "blue"
        },

        {
            id: "protect",
            icon: "🔐",
            title: "Protect PDF",
            description: "Add password protection",
            color: "purple"
        },

        {
            id: "excelToPdf",
            icon: "📊",
            title: "Excel to PDF",
            description: "Convert Excel files into PDF",
            color: "green"
        },

        {
            id: "pdfToExcel",
            icon: "📊",
            title: "PDF to Excel",
            description: "Convert PDF tables to Excel",
            color: "green"
        },

        {
            id: "pdfFormField",
            icon: "📝",
            title: "PDF Form Fields",
            description: "Create and fill PDF form fields",
            color: "blue"
        },

        {
            id: "wordToPdf",
            icon: "📄",
            title: "Word to PDF",
            description: "Convert your Word document into a PDF file",
            color: "purple"
        },

        {
            id: "search",
            icon: "🔍",
            title: "Search PDF",
            description: "Search for words and phrases in your PDF",
            color: "blue"
        },

        {
            id: "ocr",
            icon: "🔎",
            title: "OCR PDF",
            description: "Extract text from scanned PDF documents",
            color: "purple"
        },

        {
            id: "pdfPrint",
            icon: "🖨️",
            title: "Print PDF",
            description: "Print your PDF document",
            color: "blue"
        },

        {
            id: "pdfProperties",
            icon: "📄",
            title: "PDF Properties",
            description: "View PDF information and metadata",
            color: "blue"
        },

        {
            id: "signature",
            icon: "✍️",
            title: "Add Signature",
            description: "Add a signature to your PDF",
            color: "green"
        }

    ];


    // ============================================================
    // FILTER TOOLS
    // ============================================================

    const filteredFeatureCards =
        featureCards.filter((feature) => {

            const search =
                toolSearch
                    .trim()
                    .toLowerCase();

            if (!search) {
                return true;
            }

            return (
                feature.title
                    .toLowerCase()
                    .includes(search)
                ||
                feature.description
                    .toLowerCase()
                    .includes(search)
                ||
                feature.id
                    .toLowerCase()
                    .includes(search)
            );

        });


    // ============================================================
    // RENDER
    // ============================================================

    return (

        <div className="app">

            <div className="mainContent">

                <Header />


                {/* =================================================
                    DASHBOARD
                ================================================= */}

                {currentPage === "dashboard" && (

                    <div className="dashboard">


                        {/* =================================================
                            HERO
                        ================================================= */}

                        <div className="dashboardHero">

                            <div className="heroBadge">
                                ✨ Smart PDF Workspace
                            </div>

                            <h1>
                                Manage Your PDFs
                                <br />

                                <span>
                                    Smarter & Faster
                                </span>
                            </h1>

                            <p>
                                Upload, merge, split, rotate,
                                watermark and manage your PDF
                                documents in one place.
                            </p>

                        </div>


                        {/* =================================================
                            STATISTICS
                        ================================================= */}

                        <div className="dashboardStats">

                            <div className="statCard">

                                <div className="statIcon">
                                    📚
                                </div>

                                <div>

                                    <strong>
                                        {pdfList.length}
                                    </strong>

                                    <span>
                                        Total PDFs
                                    </span>

                                </div>

                            </div>


                            <div className="statCard">

                                <div className="statIcon">
                                    ⚡
                                </div>

                                <div>

                                    <strong>
                                        {featureCards.length}
                                    </strong>

                                    <span>
                                        PDF Tools
                                    </span>

                                </div>

                            </div>


                            <div className="statCard">

                                <div className="statIcon">
                                    🔒
                                </div>

                                <div>

                                    <strong>
                                        Secure
                                    </strong>

                                    <span>
                                        Local Workspace
                                    </span>

                                </div>

                            </div>

                        </div>


                        {/* =================================================
                            UPLOAD
                        ================================================= */}

                        <div className="dashboardUploadSection">

                            <div className="sectionTitle">

                                <h2>
                                    📤 Upload a PDF
                                </h2>

                                <p>
                                    Start by uploading a document
                                    to your workspace.
                                </p>

                            </div>


                            <Upload
                                onUploadSuccess={() => {
                                    loadPdfs();
                                }}
                            />

                        </div>


                        {/* =================================================
                            PDF TOOLS
                        ================================================= */}

                        <div className="toolsSection">

                            <div className="sectionTitle">

                                <h2>
                                    🛠 PDF Tools
                                </h2>

                                <p>
                                    Choose a tool to work with
                                    your documents.
                                </p>

                            </div>


                            {/* =================================================
                                MAIN SEARCH BAR
                            ================================================= */}

                            <div
                                className="toolSearchContainer"
                                style={{
                                    display: "flex",
                                    justifyContent: "center",
                                    alignItems: "center",
                                    margin: "20px 0 30px",
                                    width: "100%"
                                }}
                            >

                                <div
                                    style={{
                                        position: "relative",
                                        width: "min(600px, 90%)"
                                    }}
                                >

                                    <span
                                        style={{
                                            position: "absolute",
                                            left: "16px",
                                            top: "50%",
                                            transform: "translateY(-50%)",
                                            fontSize: "20px",
                                            pointerEvents: "none"
                                        }}
                                    >
                                        🔍
                                    </span>


                                    <input
                                        type="text"
                                        value={toolSearch}
                                        onChange={(event) =>
                                            setToolSearch(
                                                event.target.value
                                            )
                                        }
                                        placeholder="Search PDF tools..."
                                        aria-label="Search PDF tools"
                                        style={{
                                            width: "100%",
                                            boxSizing: "border-box",
                                            padding: "14px 45px 14px 48px",
                                            borderRadius: "12px",
                                            border: "1px solid rgba(255,255,255,0.25)",
                                            outline: "none",
                                            background: "rgba(255,255,255,0.08)",
                                            color: "white",
                                            fontSize: "16px"
                                        }}
                                    />


                                    {toolSearch && (

                                        <button
                                            type="button"
                                            onClick={() =>
                                                setToolSearch("")
                                            }
                                            style={{
                                                position: "absolute",
                                                right: "10px",
                                                top: "50%",
                                                transform: "translateY(-50%)",
                                                border: "none",
                                                background: "transparent",
                                                color: "white",
                                                cursor: "pointer",
                                                fontSize: "18px"
                                            }}
                                            aria-label="Clear search"
                                        >
                                            ✕
                                        </button>

                                    )}

                                </div>

                            </div>


                            {/* =================================================
                                SEARCH RESULT COUNT
                            ================================================= */}

                            {toolSearch.trim() && (

                                <div
                                    style={{
                                        textAlign: "center",
                                        marginBottom: "20px",
                                        opacity: 0.8,
                                        color: "white"
                                    }}
                                >
                                    Showing{" "}
                                    <strong>
                                        {filteredFeatureCards.length}
                                    </strong>{" "}
                                    of{" "}
                                    <strong>
                                        {featureCards.length}
                                    </strong>{" "}
                                    PDF tools
                                </div>

                            )}


                            {/* =================================================
                                TOOL GRID
                            ================================================= */}

                            {filteredFeatureCards.length > 0 ? (

                                <div className="featureGrid">

                                    {filteredFeatureCards.map(
                                        (feature) => (

                                            <button
                                                key={feature.id}
                                                className={`featureCard ${feature.color}`}
                                                onClick={() =>
                                                    setCurrentPage(
                                                        feature.id
                                                    )
                                                }
                                            >

                                                <div className="featureIcon">
                                                    {feature.icon}
                                                </div>

                                                <div className="featureContent">

                                                    <h3>
                                                        {feature.title}
                                                    </h3>

                                                    <p>
                                                        {feature.description}
                                                    </p>

                                                </div>

                                                <div className="featureArrow">
                                                    →
                                                </div>

                                            </button>

                                        )
                                    )}

                                </div>

                            ) : (

                                <div
                                    style={{
                                        textAlign: "center",
                                        padding: "50px 20px",
                                        color: "white"
                                    }}
                                >

                                    <div
                                        style={{
                                            fontSize: "45px",
                                            marginBottom: "15px"
                                        }}
                                    >
                                        🔍
                                    </div>

                                    <h3>
                                        No PDF tool found
                                    </h3>

                                    <p>
                                        No tool matches "
                                        {toolSearch}
                                        "
                                    </p>

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setToolSearch("")
                                        }
                                        style={{
                                            marginTop: "15px",
                                            padding: "10px 20px",
                                            borderRadius: "8px",
                                            border: "none",
                                            cursor: "pointer"
                                        }}
                                    >
                                        Show All Tools
                                    </button>

                                </div>

                            )}

                        </div>


                        {/* =================================================
                            FOOTER
                        ================================================= */}

                        <div className="dashboardFooter">

                            <span>
                                📄 Smart PDF Editor
                            </span>

                            <span>
                                •
                            </span>

                            <span>
                                Your all-in-one PDF workspace
                            </span>

                        </div>

                    </div>

                )}


                {/* =================================================
                    UPLOAD
                ================================================= */}

                {currentPage === "upload" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Upload
                            onUploadSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF CROP
                ================================================= */}

                {currentPage === "pdfCrop" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfCrop
                            pdfList={pdfList}
                            onSuccess={() => {
                                loadPdfs();
                            }}
                            onBackHome={goHome}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF FORM FIELD
                ================================================= */}

                {currentPage === "pdfFormField" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfFormField
                            pdfList={pdfList}
                            onSuccess={() => {
                                console.log(
                                    "PDF form filled successfully"
                                );
                            }}
                            onBackHome={goHome}
                        />

                    </div>

                )}


                {/* =================================================
                    MERGE
                ================================================= */}

                {currentPage === "merge" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Merge
                            pdfList={pdfList}
                            onMergeSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    EDIT PDF
                ================================================= */}

                {currentPage === "edit" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <EditPdf
                            pdfList={pdfList}
                            onEditSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF LINKS
                ================================================= */}

                {currentPage === "pdfLinks" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfLinks
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF PRINT
                ================================================= */}

                {currentPage === "pdfPrint" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfPrint
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF PROPERTIES
                ================================================= */}

                {currentPage === "pdfProperties" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfProperties
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    SPLIT
                ================================================= */}

                {currentPage === "split" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Split
                            pdfList={pdfList}
                            onSplitSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    DELETE
                ================================================= */}

                {currentPage === "delete" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <DeletePdf
                            pdfList={pdfList}
                            onDelete={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    ROTATE
                ================================================= */}

                {currentPage === "rotate" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <RotatePdf
                            pdfList={pdfList}
                            onRotateSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    EXTRACT
                ================================================= */}

                {currentPage === "extract" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <ExtractPages
                            pdfList={pdfList}
                            onExtractSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    MY PDFs
                ================================================= */}

                {currentPage === "pdfs" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfTable
                            pdfList={pdfList}
                            onDelete={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    WATERMARK
                ================================================= */}

                {currentPage === "watermark" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Watermark
                            pdfList={pdfList}
                            onWatermarkSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    FIND & REPLACE
                ================================================= */}

                {currentPage === "findreplace" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <FindReplace
                            pdfList={pdfList}
                            onFindReplaceSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PREVIEW
                ================================================= */}

                {currentPage === "preview" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PreviewPdf
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF BOOKMARK
                ================================================= */}

                {currentPage === "pdfBookmark" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfBookmark
                            pdfList={pdfList}
                            onSuccess={() => {
                                loadPdfs();
                            }}
                            onBackHome={goHome}
                        />

                    </div>

                )}


                {/* =================================================
                    IMAGES TO PDF
                ================================================= */}

                {currentPage === "imageToPdf" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <ImageToPdf
                            onConversionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF PERMISSIONS
                ================================================= */}

                {currentPage === "permissions" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Permissions
                            pdfList={pdfList}
                            onPermissionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    HIGHLIGHT PDF
                ================================================= */}

                {currentPage === "highlight" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Highlight
                            pdfList={pdfList}
                            onHighlightSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    REORDER
                ================================================= */}

                {currentPage === "reorder" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <ReorderPages
                            pdfList={pdfList}
                            onReorderSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PROTECT PDF
                ================================================= */}

                {currentPage === "protect" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <ProtectPdf
                            pdfList={pdfList}
                            onProtectSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF TO WORD
                ================================================= */}

                {currentPage === "pdfToWord" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfToWord
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    DIGITAL SIGNATURE VERIFICATION
                ================================================= */}

                {currentPage === "pdfSignatureVerification" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfSignatureVerification
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    EXCEL TO PDF
                ================================================= */}

                {currentPage === "excelToPdf" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <ExcelToPdf
                            onConversionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF TO EXCEL
                ================================================= */}

                {currentPage === "pdfToExcel" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfToExcel
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    SIGNATURE
                ================================================= */}

                {currentPage === "signature" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Signature
                            pdfList={pdfList}
                            onSignatureSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    RESIZE PDF
                ================================================= */}

                {currentPage === "pdfResize" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfResize
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    COMPRESSION
                ================================================= */}

                {currentPage === "compression" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Compression
                            pdfList={pdfList}
                            onCompressionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    SEARCH PDF
                ================================================= */}

                {currentPage === "search" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <SearchPdf
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    PAGE NUMBERS
                ================================================= */}

                {currentPage === "pageNumbers" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PageNumbers
                            pdfList={pdfList}
                            onPageNumberSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    WORD TO PDF
                ================================================= */}

                {currentPage === "wordToPdf" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <WordToPdf
                            pdfList={pdfList}
                            onConversionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    OCR
                ================================================= */}

                {currentPage === "ocr" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <OcrPdf
                            pdfList={pdfList}
                        />

                    </div>

                )}


                {/* =================================================
                    PDF TO IMAGES
                ================================================= */}

                {currentPage === "pdfToImage" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <PdfToImage
                            pdfList={pdfList}
                            onConversionSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}


                {/* =================================================
                    METADATA
                ================================================= */}

                {currentPage === "metadata" && (

                    <div className="featurePage">

                        <button
                            className="backButton"
                            onClick={goHome}
                        >
                            ← Back to Home
                        </button>

                        <Metadata
                            pdfList={pdfList}
                            onMetadataSuccess={() => {
                                loadPdfs();
                            }}
                        />

                    </div>

                )}

            </div>

        </div>

    );
}


export default App;
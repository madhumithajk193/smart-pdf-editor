function Sidebar({ currentPage, setCurrentPage }) {

    return (

        <div className="sidebar">

            <h2>
                📄 Smart PDF
            </h2>

            <button
                className={
                    currentPage === "dashboard"
                        ? "active"
                        : ""
                }
                onClick={() =>
                    setCurrentPage("dashboard")
                }
            >
                🏠 Dashboard
            </button>

            <button
                className={
                    currentPage === "upload"
                        ? "active"
                        : ""
                }
                onClick={() =>
                    setCurrentPage("upload")
                }
            >
                📤 Upload PDF
            </button>

            <button
                className={
                    currentPage === "merge"
                        ? "active"
                        : ""
                }
                onClick={() =>
                    setCurrentPage("merge")
                }
            >
                🔗 Merge PDF
            </button>

            <button
                className={
                    currentPage === "split"
                        ? "active"
                        : ""
                }
                onClick={() =>
                    setCurrentPage("split")
                }
            >
                ✂️ Split PDF
            </button>

            <button
                className={
                    currentPage === "pdfs"
                        ? "active"
                        : ""
                }
                onClick={() =>
                    setCurrentPage("pdfs")
                }
            >
                📁 My PDFs
            </button>

        </div>

    );

}

export default Sidebar;
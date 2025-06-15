// Application State
let currentUser = null
let currentBoard = null
let boards = []
let lists = []
let cards = {}
let draggedCard = null
let currentListForCard = null
let collaborators = [] // Track collaborators for the current board
let currentRenamingBoard = null // Track the board being renamed
let currentRenamingList = null //: Track the list being renamed
let currentEditingCard = null // Track the card being edited

// DOM Elements
const loadingScreen = document.getElementById("loading-screen")
const authSection = document.getElementById("auth-section")
const mainApp = document.getElementById("main-app")
const loginForm = document.getElementById("login-form")
const registerForm = document.getElementById("register-form")
const boardsView = document.getElementById("boards-view")
const boardView = document.getElementById("board-view")

window.toggleListMenu = toggleListMenu;
window.deleteList = deleteList;
window.deleteCard = deleteCard;
window.showCreateCardModal = showCreateCardModal;

// Initialize Application
document.addEventListener("DOMContentLoaded", () => {
    initializeApp()
    setupEventListeners()
})

// Initialize the application
function initializeApp() {
    // Check if user is already logged in
    const token = localStorage.getItem("jwt_token")
    const userData = localStorage.getItem("user_data")

    if (token && userData) {
        try {
            currentUser = JSON.parse(userData)
            showMainApp()
            loadBoards()
        } catch (error) {
            console.error("Error parsing user data:", error)
            logout()
        }
    } else {
        showAuthSection()
    }
}

// Setup Event Listers
function setupEventListeners() {
    // Authentication
    document.getElementById("loginForm").addEventListener("submit", handleLogin)
    document.getElementById("registerForm").addEventListener("submit", handleRegister)
    document.getElementById("show-register").addEventListener("click", showRegisterForm)
    document.getElementById("show-login").addEventListener("click", showLoginForm)
    document.getElementById("logout-btn").addEventListener("click", logout)

    // Board Management
    document.getElementById("rename-board-form").addEventListener("submit", handleRenameBoard);
    document.getElementById("create-board-btn").addEventListener("click", showCreateBoardModal)
    document.getElementById("create-board-form").addEventListener("submit", handleCreateBoard)
    document.getElementById("back-to-boards").addEventListener("click", showBoardsView)

    // List Management
    document.getElementById("create-list-btn").addEventListener("click", showCreateListModal)
    document.getElementById("create-list-form").addEventListener("submit", handleCreateList)

    // Card Management
    document.getElementById("create-card-form").addEventListener("submit", handleCreateCard)
    document.getElementById("edit-card-form").addEventListener("submit", async function (e) {
        e.preventDefault()
        const title = document.getElementById("edit-card-title-input").value
        const description = document.getElementById("edit-card-description-input").value
        const submitBtn = e.target.querySelector('button[type="submit"]')
        setButtonLoading(submitBtn, true)
        try {
            await window.apiService.updateCard(currentEditingCard.id, { title, description })
            // Update local state
            const listCards = cards[currentEditingCard.listId]
            const cardIndex = listCards.findIndex((c) => c.id === currentEditingCard.id)
            if (cardIndex !== -1) {
                listCards[cardIndex] = { ...listCards[cardIndex], title, description }
            }
            renderLists()
            hideModal("edit-card-modal")
            document.getElementById("edit-card-form").reset()
            showSuccessToast("Card updated successfully!")
        } catch (error) {
            showErrorToast("Failed to update card")
        } finally {
            setButtonLoading(submitBtn, false)
        }
    })

    // Modal Management
    setupModalListeners()

    document.getElementById("rename-board-btn").addEventListener("click", function () {
        if (currentBoard) {
            showRenameBoardModal(currentBoard);
        }
    });
    document.getElementById("share-board-btn").addEventListener("click", showShareBoardModal);
    document.getElementById("share-board-form").addEventListener("submit", handleShareBoard);
}

// Authentication Functions
async function handleLogin(e) {
    e.preventDefault()

    const username = document.getElementById("login-username").value
    const password = document.getElementById("login-password").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)
    hideError()

    try {
        const response = await window.apiService.login({ username, password })

        // Store authentication data
        localStorage.setItem("jwt_token", response.token)
        localStorage.setItem("user_data", JSON.stringify(response.user))
        currentUser = response.user

        showMainApp()
        loadBoards()
        showSuccessToast("Welcome back!")
    } catch (error) {
        showError("Invalid username or password")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

async function handleRegister(e) {
    e.preventDefault()

    const username = document.getElementById("register-username").value
    const email = document.getElementById("register-email").value
    const password = document.getElementById("register-password").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)
    hideError()

    try {
        await window.apiService.register({ username, email, password })

        // Auto-login after registration
        const loginResponse = await window.apiService.login({ username, password })
        localStorage.setItem("jwt_token", loginResponse.token)
        localStorage.setItem("user_data", JSON.stringify(loginResponse.user))
        currentUser = loginResponse.user

        showMainApp()
        loadBoards()
        showSuccessToast("Account created successfully!")
    } catch (error) {
        showError("Registration failed. Please try again.")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

function logout() {
    localStorage.removeItem("jwt_token")
    localStorage.removeItem("user_data")
    currentUser = null
    currentBoard = null
    boards = []
    lists = []
    cards = {}

    showAuthSection()
    showSuccessToast("Signed out successfully")
}

// Board Management Functions
async function loadBoards() {
    try {
        boards = await window.apiService.getBoards()
        renderBoards()
    } catch (error) {
        showErrorToast("Failed to load boards")
        console.error("Error loading boards:", error)
    }
}

async function handleCreateBoard(e) {
    e.preventDefault()

    const title = document.getElementById("board-title-input").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)

    try {
        const newBoard = await window.apiService.createBoard({ title })
        boards.push(newBoard)
        renderBoards()
        hideModal("create-board-modal")
        document.getElementById("create-board-form").reset()
        showSuccessToast("Board created successfully!")
    } catch (error) {
        showErrorToast("Failed to create board")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

async function deleteBoard(boardId) {
    if (!confirm("Are you sure you want to delete this board?")) return

    try {
        await window.apiService.deleteBoard(boardId)
        boards = boards.filter((board) => board.id !== boardId)
        renderBoards()
        showSuccessToast("Board deleted successfully")
    } catch (error) {
        showErrorToast("Failed to delete board")
    }
}

function renderBoards() {
    const boardsGrid = document.getElementById("boards-grid")
    boardsGrid.innerHTML = ""

    // Create board card
    const createCard = document.createElement("div")
    createCard.className = "board-card create-board-card"
    createCard.innerHTML = `
        <i class="fas fa-plus"></i>
        <span>Create New Board</span>
    `
    createCard.addEventListener("click", showCreateBoardModal)
    boardsGrid.appendChild(createCard)

    // Render existing boards
    boards.forEach((board) => {
        const boardCard = document.createElement("div")
        boardCard.className = "board-card"
        boardCard.innerHTML = `
            <div class="board-actions">
                <button class="board-action-btn" onclick="showRenameBoardModal(${board.id})" title="Rename Board">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="board-action-btn danger" onclick="deleteBoard(${board.id})" title="Delete Board">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
            <h3>${escapeHtml(board.title)}</h3>
            <div class="board-meta">
                <i class="fas fa-calendar"></i>
                ${new Date(board.createdAt).toLocaleDateString()}
            </div>
        `
        boardCard.addEventListener("click", (e) => {
            if (!e.target.closest(".board-actions")) {
                openBoard(board)
            }
        })
        boardsGrid.appendChild(boardCard)
    })
}

// List Management Functions
async function openBoard(board) {
    currentBoard = board
    document.getElementById("board-title").textContent = board.title

    try {
        await loadBoardData()
        showBoardView()
    } catch (error) {
        showErrorToast("Failed to load board data")
        console.error("Error loading board data:", error)
    }
}

async function loadBoardData() {
    try {
        lists = await window.apiService.getListsForBoard(currentBoard.id)
        lists.sort((a, b) => a.position - b.position)

        // Load cards for each list
        cards = {}
        for (const list of lists) {
            const listCards = await window.apiService.getCardsForList(list.id)
            cards[list.id] = listCards.sort((a, b) => a.position - b.position)
        }

        renderLists()
        await loadCollaborators() // Load collaborators when board data is loaded
    } catch (error) {
        throw error
    }
}

async function handleCreateList(e) {
    e.preventDefault()

    const title = document.getElementById("list-title-input").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)

    try {
        const newList = await window.apiService.createList({
            title,
            position: lists.length,
            boardId: currentBoard.id,
        })

        lists.push(newList)
        cards[newList.id] = []
        renderLists()
        hideModal("create-list-modal")
        document.getElementById("create-list-form").reset()
        showSuccessToast("List created successfully!")
    } catch (error) {
        showErrorToast("Failed to create list")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

async function deleteList(listId) {
    if (!confirm("Are you sure you want to delete this list and all its cards?")) return

    try {
        await window.apiService.deleteList(listId)
        lists = lists.filter((list) => list.id !== listId)
        delete cards[listId]
        renderLists()
        showSuccessToast("List deleted successfully")
    } catch (error) {
        showErrorToast("Failed to delete list")
    }
}

function renderLists() {
    const listsContainer = document.getElementById("lists-container")
    listsContainer.innerHTML = ""

    lists.forEach((list) => {
        const listElement = createListElement(list)
        listsContainer.appendChild(listElement)
    })
}

function createListElement(list) {
    const listDiv = document.createElement("div")
    listDiv.className = "list-column"
    listDiv.dataset.listId = list.id

    const listIndex = lists.findIndex((l) => l.id === list.id)
    const canMoveLeft = listIndex > 0
    const canMoveRight = listIndex < lists.length - 1

    listDiv.innerHTML = `
        <div class="list-header">
            <h3 class="list-title">${escapeHtml(list.title)}</h3>
            <div class="list-menu">
                <button class="list-menu-btn" onclick="toggleListMenu(${list.id})">
                    <i class="fas fa-ellipsis-h"></i>
                </button>
                <div id="list-menu-${list.id}" class="list-menu-dropdown hidden">
                    <button class="list-menu-item" onclick="showRenameListModal(${list.id})">
                        <i class="fas fa-edit"></i> Rename List
                    </button>
                    <button class="list-menu-item danger" onclick="deleteList(${list.id})">
                        <i class="fas fa-trash"></i> Delete List
                    </button>
                </div>
            </div>
        </div>
        <div class="list-move-buttons">
            <button class="list-move-btn" onclick="moveListLeft(${list.id})" ${!canMoveLeft ? "disabled" : ""}>
                <i class="fas fa-arrow-left"></i> Move Left
            </button>
            <button class="list-move-btn" onclick="moveListRight(${list.id})" ${!canMoveRight ? "disabled" : ""}>
                <i class="fas fa-arrow-right"></i> Move Right
            </button>
        </div>
        <div class="cards-container" id="cards-${list.id}">
            ${renderCards(list.id)}
        </div>
        <button class="add-card-btn" onclick="showCreateCardModal(${list.id})">
            <i class="fas fa-plus"></i> Add a card
        </button>
    `
    setupListDragAndDrop(listDiv, list.id)
    return listDiv
}

function renderCards(listId) {
    const listCards = cards[listId] || []
    return listCards
        .map(
            (card) => `
        <div class="card" draggable="true" data-card-id="${card.id}" data-list-id="${listId}">
            <div class="card-actions">
                <button class="card-action-btn edit" onclick="showEditCardModal(${card.id}, ${listId})" title="Edit Card">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="card-action-btn delete" onclick="deleteCard(${card.id}, ${listId})" title="Delete Card">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
            <div class="card-title">${escapeHtml(card.title)}</div>
            ${card.description ? `<div class="card-description">${escapeHtml(card.description)}</div>` : ""}
        </div>
    `,
        )
        .join("")
}

// Card Management Functions
async function handleCreateCard(e) {
    e.preventDefault()

    const title = document.getElementById("card-title-input").value
    const description = document.getElementById("card-description-input").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)

    try {
        const listCards = cards[currentListForCard] || []
        const newCard = await window.apiService.createCard({
            title,
            description: description || undefined,
            position: listCards.length,
            listId: currentListForCard,
        })

        cards[currentListForCard].push(newCard)
        renderLists()
        hideModal("create-card-modal")
        document.getElementById("create-card-form").reset()
        showSuccessToast("Card created successfully!")
    } catch (error) {
        showErrorToast("Failed to create card")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

async function deleteCard(cardId, listId) {
    if (!confirm("Are you sure you want to delete this card?")) return

    try {
        await window.apiService.deleteCard(cardId)
        cards[listId] = cards[listId].filter((card) => card.id !== cardId)
        renderLists()
        showSuccessToast("Card deleted successfully")
    } catch (error) {
        showErrorToast("Failed to delete card")
    }
}

window.showEditCardModal = function (cardId, listId) {
    const card = (cards[listId] || []).find(c => c.id === cardId)
    if (!card) return
    currentEditingCard = { ...card, listId }
    document.getElementById("edit-card-title-input").value = card.title
    document.getElementById("edit-card-description-input").value = card.description || ""
    showModal("edit-card-modal")
    document.getElementById("edit-card-title-input").focus()
}

// Board Sharing Functions - NEW
async function handleShareBoard(e) {
    e.preventDefault()

    const userId = Number.parseInt(document.getElementById("collaborator-user-id").value)
    const role = document.getElementById("collaborator-role").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)

    try {
        await window.apiService.shareBoard(currentBoard.id, userId, role)
        await loadCollaborators()
        document.getElementById("share-board-form").reset()
        showSuccessToast("Collaborator added successfully!")
    } catch (error) {
        showErrorToast("Failed to add collaborator")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

async function loadCollaborators() {
    try {
        collaborators = await window.apiService.getCollaborators(currentBoard.id)
        renderCollaborators()
    } catch (error) {
        console.error("Error loading collaborators:", error)
        collaborators = []
        renderCollaborators()
    }
}

async function updateCollaboratorRole(userId, newRole) {
    try {
        await window.apiService.updateCollaboratorRole(currentBoard.id, userId, newRole);
        await loadCollaborators();
        showSuccessToast("Collaborator role updated!");
    } catch (error) {
        showErrorToast("Failed to update collaborator role");
    }
}

async function removeCollaborator(userId) {
    if (!confirm("Are you sure you want to remove this collaborator?")) return;
    try {
        await window.apiService.removeCollaborator(currentBoard.id, userId);
        await loadCollaborators();
        showSuccessToast("Collaborator removed successfully!");
    } catch (error) {
        showErrorToast("Failed to remove collaborator");
    }
}

function renderCollaborators() {
    const collaboratorsList = document.getElementById("current-collaborators");
    if (!collaborators || collaborators.length === 0) {
        collaboratorsList.innerHTML = '<div class="empty-collaborators">No collaborators yet</div>';
        return;
    }
    collaboratorsList.innerHTML = collaborators.map(user => `
        <div class="collaborator-item">
            <div class="collaborator-info">
                <div class="collaborator-id">User ID: ${user.userId}</div>
                <div class="collaborator-role ${user.role === 'EDITOR' ? 'editor' : 'viewer'}">
                    ${user.role === 'EDITOR' ? 'Editor' : 'Viewer'}
                </div>
            </div>
            <div class="collaborator-actions">
                <button class="btn btn-secondary btn-small" onclick="updateCollaboratorRole(${user.userId}, '${user.role === 'EDITOR' ? 'VIEWER' : 'EDITOR'}')">
                    <i class="fas fa-exchange-alt"></i> Set as ${user.role === 'EDITOR' ? 'Viewer' : 'Editor'}
                </button>
                <button class="btn btn-danger btn-small" onclick="removeCollaborator(${user.userId})">
                    <i class="fas fa-trash"></i> Remove
                </button>
            </div>
        </div>
    `).join("");
}

// Share Board Modal 
function showShareBoardModal() {
    showModal("share-board-modal");
    loadCollaborators();
    document.getElementById("collaborator-user-id").focus();
}

// Board Management Functions 
async function handleRenameBoard(e) {
    e.preventDefault()

    const newTitle = document.getElementById("rename-board-input").value
    const submitBtn = e.target.querySelector('button[type="submit"]')

    setButtonLoading(submitBtn, true)

    try {
        const updatedBoard = await window.apiService.updateBoard(currentRenamingBoard.id, { title: newTitle })

        const boardIndex = boards.findIndex((b) => b.id === currentRenamingBoard.id)
        if (boardIndex !== -1) {
            boards[boardIndex] = { ...boards[boardIndex], title: newTitle }
        }

        if (currentBoard && currentBoard.id === currentRenamingBoard.id) {
            currentBoard.title = newTitle
            document.getElementById("board-title").textContent = newTitle
        }

        renderBoards()
        hideModal("rename-board-modal")
        document.getElementById("rename-board-form").reset()
        showSuccessToast("Board renamed successfully!")
    } catch (error) {
        showErrorToast("Failed to rename board")
    } finally {
        setButtonLoading(submitBtn, false)
    }
}

function showRenameBoardModal(boardOrId) {
    let board = boardOrId;
    if (typeof boardOrId === "number") {
        board = boards.find(b => b.id === boardOrId);
    }
    if (!board) return;
    currentRenamingBoard = board;
    document.getElementById("rename-board-input").value = board.title;
    showModal("rename-board-modal");
    document.getElementById("rename-board-input").focus();
}
window.showRenameBoardModal = showRenameBoardModal;

// Rename List Modal 
window.showRenameListModal = function (listId) {
    const list = lists.find(l => l.id === listId)
    if (!list) return
    currentRenamingList = list
    document.getElementById("rename-list-input").value = list.title
    showModal("rename-list-modal")
    document.getElementById("rename-list-input").focus()
}

// Move List Functions 
window.moveListLeft = async function (listId) {
    const listIndex = lists.findIndex((l) => l.id === listId)
    if (listIndex <= 0) return
    await window.apiService.moveList(listId, listIndex - 1)
    // Update local state
    const list = lists[listIndex]
    lists.splice(listIndex, 1)
    lists.splice(listIndex - 1, 0, list)
    lists.forEach((list, index) => { list.position = index })
    renderLists()
}

window.moveListRight = async function (listId) {
    const listIndex = lists.findIndex((l) => l.id === listId)
    if (listIndex >= lists.length - 1) return
    await window.apiService.moveList(listId, listIndex + 1)
    // Update local state
    const list = lists[listIndex]
    lists.splice(listIndex, 1)
    lists.splice(listIndex + 1, 0, list)
    lists.forEach((list, index) => { list.position = index })
    renderLists()
}

// Drag and Drop Functions
function setupListDragAndDrop(listElement, listId) {
    const cardsContainer = listElement.querySelector(".cards-container")

    // Setup drag events for cards
    cardsContainer.addEventListener("dragstart", handleCardDragStart)
    cardsContainer.addEventListener("dragover", handleCardDragOver)
    cardsContainer.addEventListener("drop", handleCardDrop)
    cardsContainer.addEventListener("dragenter", handleCardDragEnter)
    cardsContainer.addEventListener("dragleave", handleCardDragLeave)
}

function handleCardDragStart(e) {
    if (e.target.classList.contains("card")) {
        draggedCard = {
            id: Number.parseInt(e.target.dataset.cardId),
            listId: Number.parseInt(e.target.dataset.listId),
            element: e.target,
        }
        e.target.classList.add("dragging")
        e.dataTransfer.effectAllowed = "move"
    }
}

function handleCardDragOver(e) {
    e.preventDefault()
    e.dataTransfer.dropEffect = "move"
}

function handleCardDragEnter(e) {
    e.preventDefault()
    if (e.currentTarget.classList.contains("cards-container")) {
        e.currentTarget.parentElement.classList.add("drag-over")
    }
}

function handleCardDragLeave(e) {
    if (e.currentTarget.classList.contains("cards-container")) {
        e.currentTarget.parentElement.classList.remove("drag-over")
    }
}

async function handleCardDrop(e) {
    e.preventDefault()

    if (!draggedCard) return

    const targetListElement = e.currentTarget.parentElement
    const targetListId = Number.parseInt(targetListElement.dataset.listId)

    targetListElement.classList.remove("drag-over")
    draggedCard.element.classList.remove("dragging")

    if (draggedCard.listId !== targetListId) {
        try {
            await window.apiService.moveCard(draggedCard.id, targetListId)

            // Update local state
            const card = cards[draggedCard.listId].find((c) => c.id === draggedCard.id)
            cards[draggedCard.listId] = cards[draggedCard.listId].filter((c) => c.id !== draggedCard.id)
            card.listId = targetListId
            cards[targetListId].push(card)

            renderLists()
            showSuccessToast("Card moved successfully")
        } catch (error) {
            showErrorToast("Failed to move card")
        }
    }

    draggedCard = null
}

// UI Helper Functions
function showAuthSection() {
    hideLoading()
    authSection.classList.remove("hidden")
    mainApp.classList.add("hidden")
}

function showMainApp() {
    hideLoading()
    authSection.classList.add("hidden")
    mainApp.classList.remove("hidden")
    document.getElementById("user-welcome").textContent = `Welcome back, ${currentUser.username}!`
}

function showBoardsView() {
    boardsView.classList.remove("hidden")
    boardView.classList.add("hidden")
    currentBoard = null
}

function showBoardView() {
    boardsView.classList.add("hidden")
    boardView.classList.remove("hidden")
}

function showLoginForm() {
    loginForm.classList.remove("hidden")
    registerForm.classList.add("hidden")
    hideError()
}

function showRegisterForm() {
    loginForm.classList.add("hidden")
    registerForm.classList.remove("hidden")
    hideError()
}

function hideLoading() {
    loadingScreen.classList.add("hidden")
}

// Modal Functions
function setupModalListeners() {
    // Close modal when clicking outside or on close button
    document.querySelectorAll(".modal").forEach((modal) => {
        modal.addEventListener("click", (e) => {
            if (e.target === modal) {
                hideModal(modal.id)
            }
        })
    })

    document.querySelectorAll(".modal-close, .modal-cancel").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            const modal = e.target.closest(".modal")
            hideModal(modal.id)
        })
    })
}

function showModal(modalId) {
    document.getElementById(modalId).classList.remove("hidden")
}

function hideModal(modalId) {
    document.getElementById(modalId).classList.add("hidden")
}

function showCreateBoardModal() {
    showModal("create-board-modal")
    document.getElementById("board-title-input").focus()
}

function showCreateListModal() {
    showModal("create-list-modal")
    document.getElementById("list-title-input").focus()
}

function showCreateCardModal(listId) {
    currentListForCard = listId
    showModal("create-card-modal")
    document.getElementById("card-title-input").focus()
}

// Menu Functions
function toggleListMenu(listId) {
    const menu = document.getElementById(`list-menu-${listId}`)
    const isHidden = menu.classList.contains("hidden")

    // Hide all other menus
    document.querySelectorAll(".list-menu-dropdown").forEach((m) => m.classList.add("hidden"))

    if (isHidden) {
        menu.classList.remove("hidden")
    }
}

// Close menus when clicking outside
document.addEventListener("click", (e) => {
    if (!e.target.closest(".list-menu")) {
        document.querySelectorAll(".list-menu-dropdown").forEach((m) => m.classList.add("hidden"))
    }
})

// Toast Functions
function showSuccessToast(message) {
    showToast(message, "success")
}

function showErrorToast(message) {
    showToast(message, "error")
}

function showToast(message, type) {
    const toastId = type === "success" ? "success-toast" : "error-toast"
    const messageId = type === "success" ? "success-message" : "error-message"

    document.getElementById(messageId).textContent = message
    document.getElementById(toastId).classList.remove("hidden")

    setTimeout(() => {
        document.getElementById(toastId).classList.add("hidden")
    }, 3000)
}

// Error Handling Functions
function showError(message) {
    const errorDiv = document.getElementById("auth-error")
    errorDiv.textContent = message
    errorDiv.classList.remove("hidden")
}

function hideError() {
    document.getElementById("auth-error").classList.add("hidden")
}

// Utility Functions
function setButtonLoading(button, loading) {
    if (loading) {
        button.classList.add("loading")
        button.disabled = true
    } else {
        button.classList.remove("loading")
        button.disabled = false
    }
}

function escapeHtml(text) {
    const div = document.createElement("div")
    div.textContent = text
    return div.innerHTML
}

// Global error handler
window.addEventListener("error", (e) => {
    console.error("Global error:", e.error)
    showErrorToast("An unexpected error occurred")
})

// Handle unhandled promise rejections
window.addEventListener("unhandledrejection", (e) => {
    console.error("Unhandled promise rejection:", e.reason)
    showErrorToast("An unexpected error occurred")
})

document.getElementById("rename-list-form").addEventListener("submit", async function (e) {
    e.preventDefault()
    const newTitle = document.getElementById("rename-list-input").value
    const submitBtn = e.target.querySelector('button[type="submit"]')
    setButtonLoading(submitBtn, true)
    try {
        await window.apiService.updateList(currentRenamingList.id, { title: newTitle })
        // Update local state
        const listIndex = lists.findIndex((l) => l.id === currentRenamingList.id)
        if (listIndex !== -1) {
            lists[listIndex].title = newTitle
        }
        renderLists()
        hideModal("rename-list-modal")
        document.getElementById("rename-list-form").reset()
        showSuccessToast("List renamed successfully!")
    } catch (error) {
        showErrorToast("Failed to rename list")
    } finally {
        setButtonLoading(submitBtn, false)
    }
})

window.updateCollaboratorRole = updateCollaboratorRole;
window.removeCollaborator = removeCollaborator;

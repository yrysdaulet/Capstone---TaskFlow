// TaskFlow API Service

// API Configuration
const API_BASE_URL = "http://localhost:8080"

// API Service Class - Pure JavaScript
class ApiService {
    constructor() {
        this.baseURL = API_BASE_URL
    }

    // Get authentication headers
    getAuthHeaders() {
        const token = localStorage.getItem("jwt_token")
        const headers = {
            "Content-Type": "application/json",
        }

        if (token) {
            headers["Authorization"] = `Bearer ${token}`
        }

        return headers
    }

    // Generic request method - Pure JavaScript fetch
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`

        const config = {
            headers: this.getAuthHeaders(),
            ...options,
        }

        try {
            const response = await fetch(url, config)

            if (!response.ok) {
                const errorText = await response.text()
                throw new Error(`API Error: ${response.status} - ${errorText}`)
            }

            // Handle empty responses
            const contentType = response.headers.get("content-type")
            if (contentType && contentType.includes("application/json")) {
                return await response.json()
            }

            return null
        } catch (error) {
            console.error("API Request failed:", error)
            throw error
        }
    }

    // Authentication endpoints
    async login(credentials) {
        const response = await this.request("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(credentials),
        })

        // Handle different response formats
        if (response.user) {
            // Format: { token: "...", user: { id, username, email } }
            return response
        } else if (response.userId) {
            // Format: { token: "...", userId: 9, username: "..." }
            return {
                token: response.token,
                user: {
                    id: response.userId,
                    username: response.username,
                    email: response.email || "",
                },
            }
        }

        throw new Error("Invalid response format")
    }

    async register(userData) {
        return await this.request("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(userData),
        })
    }

    // Board endpoints
    async getBoards() {
        return await this.request("/api/boards")
    }

    async createBoard(boardData) {
        return await this.request("/api/boards", {
            method: "POST",
            body: JSON.stringify(boardData),
        })
    }

    async updateBoard(id, boardData) {
        return await this.request(`/api/boards/${id}`, {
            method: "PUT",
            body: JSON.stringify(boardData),
        })
    }

    async deleteBoard(id) {
        return await this.request(`/api/boards/${id}`, {
            method: "DELETE",
        })
    }

    async getBoardById(id) {
        return await this.request(`/api/boards/${id}`)
    }

    // List endpoints
    async getListsForBoard(boardId) {
        return await this.request(`/api/lists/board/${boardId}`)
    }

    async createList(listData) {
        return await this.request("/api/lists", {
            method: "POST",
            body: JSON.stringify(listData),
        })
    }

    async updateList(id, listData) {
        return await this.request(`/api/lists/${id}`, {
            method: "PUT",
            body: JSON.stringify(listData),
        })
    }

    async deleteList(id) {
        return await this.request(`/api/lists/${id}`, {
            method: "DELETE",
        })
    }

    async moveList(id, newPosition) {
        return await this.request(`/api/lists/${id}/move?newPosition=${newPosition}`, {
            method: "POST",
        })
    }

    // Card endpoints
    async getCardsForList(listId) {
        return await this.request(`/api/cards/list/${listId}`)
    }

    async createCard(cardData) {
        return await this.request("/api/cards", {
            method: "POST",
            body: JSON.stringify(cardData),
        })
    }

    async updateCard(id, cardData) {
        return await this.request(`/api/cards/${id}`, {
            method: "PUT",
            body: JSON.stringify(cardData),
        })
    }

    async deleteCard(id) {
        return await this.request(`/api/cards/${id}`, {
            method: "DELETE",
        })
    }

    async moveCard(id, newListId, newPosition = null) {
        let url = `/api/cards/${id}/move?newListId=${newListId}`
        if (newPosition !== null) {
            url += `&newPosition=${newPosition}`
        }
        return await this.request(url, {
            method: "POST",
        })
    }

    // Collaboration endpoints - NEW
    async shareBoard(boardId, userId, role) {
        return await this.request("/api/collaborations", {
            method: "POST",
            body: JSON.stringify({
                boardId: boardId,
                userId: userId,
                role: role, // "VIEWER" or "EDITOR"
            }),
        })
    }

    async updateCollaboratorRole(boardId, userId, role) {
        return await this.request("/api/collaborations", {
            method: "PUT",
            body: JSON.stringify({
                boardId: boardId,
                userId: userId,
                role: role,
            }),
        })
    }

    async getCollaborators(boardId) {
        return await this.request(`/api/collaborations/board/${boardId}`)
    }

    async removeCollaborator(boardId, userId) {
        return await this.request(`/api/collaborations`, {
            method: "DELETE",
            body: JSON.stringify({
                boardId: boardId,
                userId: userId,
            }),
        })
    }
}

// Create global API instance - Pure JavaScript
const apiService = new ApiService()
window.apiService = apiService

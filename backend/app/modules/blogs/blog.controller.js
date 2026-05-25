const prisma = require("../../config/db")
const blogService = require("./blog.service")

module.exports.getPosts = async (req, res) => {
    await blogService.getAllPosts(res)
}

module.exports.getPostById = async (req, res) => {
    await blogService.getPostById(req, res)
}

module.exports.getPostFilteredSearched = async (req, res) => {
    await blogService.getPostFilteredSearched(req, res)
}

module.exports.createPost = async (req, res) => {
    await blogService.createPost(req, res)
}

module.exports.updatePost = async (req, res) => {
    await blogService.updatePost(req, res)
}

module.exports.deletePost = async (req, res) => {
    await blogService.deletePost(req, res)
}
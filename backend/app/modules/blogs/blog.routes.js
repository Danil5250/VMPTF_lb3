const express = require('express')
const router = express.Router()
const controller = require('./blog.controller')
const prisma = require("../../config/db")
const { cacheMiddleware } = require("../middleware/cashing")


router.get('/qs', cacheMiddleware, controller.getPostFilteredSearched)
router.get('/:id', controller.getPostById)
router.get('/', controller.getPosts)
router.post('/', controller.createPost)
router.put('/:id', controller.updatePost)
router.delete('/:id', controller.deletePost)

module.exports = router
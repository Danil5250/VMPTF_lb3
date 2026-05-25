const express = require('express')
const router = express.Router()

const controller = require('./comments.controller')

router.get('/', controller.getCommentsByPost)
router.post('/', controller.createComment)
router.delete('/:id', controller.deleteComment)
router.put('/:id', controller.editComment)

module.exports = router
require('dotenv').config()
const express = require('express')
const cors = require('cors')

const app = express()
app.use(cors({
    origin: process.env.FRONTEND_URL, // without this backend accepts all requests, but with it only from the specified URL
    credentials: true //for sending cookies and auth
}))

app.use(express.json()) // for parsing json in request body

app.use('/api/blogs', require('./modules/blogs/blog.routes'))
app.use('/api/categories', require('./modules/categories/categories.routes'))
app.use('/api/comments', require('./modules/comments/comments.routes'))
app.use('/api/auth', require('./modules/auth/auth.routes'))

module.exports = app
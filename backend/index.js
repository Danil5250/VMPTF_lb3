const app = require('./app/app')
const serverConfig = require('./app/config/serverConfig')



const PORT = serverConfig.port;
app.listen(PORT,  () => {
    console.log(`Server is running on port ${PORT}`)
})
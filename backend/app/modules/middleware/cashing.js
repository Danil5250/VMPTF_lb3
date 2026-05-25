const NodeCache = require('node-cache');

// Кеш із часом життя 100 секунд
const myCache = new NodeCache({ stdTTL: 100 });

// Middleware для кешування
module.exports.cacheMiddleware = (req, res, next) => {
  const key = req.originalUrl; // Використовуємо URL як ключ
  const cachedResponse = myCache.get(key);

  if (cachedResponse) {
    console.log('Повертаємо з кешу');
    return res.send(cachedResponse);
  }

  // Перехоплюємо оригінальний res.send, щоб зберегти дані в кеш (додаткова зміна originalSend)
  res.originalSend = res.send;
  //отримуємо відповідь сервера, оскільки кеш пустий
  res.send = (body) => {
    // записуємо в кеш
    myCache.set(key, body);
    //відравляємо відповідь
    res.originalSend(body);
  };
  
  //виклик настпуної функції
  next();
};

module.exports.clearCache = () => {
  myCache.flushAll();
};

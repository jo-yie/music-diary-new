// module.exports = [
//     {
//         context: ['/'], 
//         target: 'http://localhost:8080', 
//         secure: false, 
        
//     }
// ]

module.exports = [
    {
      context: ['/api'],
      target: 'http://localhost:8080',
      secure: false,
    }
  ];
const request = require('request');
const cheerio = require('cheerio');

request('https://na.op.gg/summoner/userName=%5BUSERNAME', 
(error, response, html) => {
    if(!error && response.statusCode == 200){
        const $ = cheerio.load(html);
        $('.l-container').each((i,el) => {
            const buttonText = $(el)
            .find('.Profile')
            .text();
            console.log(buttonText);
        });
    };
});
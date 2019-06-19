const request = require('request');
const cheerio = require('cheerio');
const writeStream = fs.createWriteStream('post.csv')

//Write Headers
writeStream.write('Title, Link, Date, \n')

request('https://na.op.gg/summoner/userName=%5BUSERNAME', 
(error, response, html) => {
    if(!error && response.statusCode == 200){
        const $ = cheerio.load(html);

        const menu = $('.menu');
        console.log(menu);
        console.log(menu.html);
        console.log(menu.text());

        const output = siteHeading.find('h1').text();
        const output = siteHeading.children('h1').text();
        const output = siteheading.children('h1').next().text();
        const output = siteheading.children('h1').parent().text();
        $('.nav-item a').each((i, el) => {
            const item = $(el).text();
            const link = $(el).attr('href')
            console.log(item);
            console.log(link);
            //for loop to get the information for each item under a heading
        });
        $('.post-preview').each((i,el) => {
            const title = $(el)
            .find('.post-title')
            .text()
            .replace(/\s\s+/g, '');

            const link = $(el)
            .find('a')
            .attr('href');
            const date = $el.find('post-date')
            .text();
            .replace(/,/, ' ')

            //Write Row to CSV
            writeStream.write('${title}, ${link}, ${date} \n')
            console.log('Scraping Done...')
            console.log(title, link, date)
            console.log(title);
        });
        
        
    }
});

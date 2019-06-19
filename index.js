/* let cheerio = require('cheerio')
let jsonframe = require('jsonframe-cheerio')
let $ = cheerio.load('na.op.gg/ranking/ladder')
jsonframe($);

const request = require('request')

var frame = {
    "Item1": ".item",
    "data": [{
        "element1": ""
    }]
}

var tempObj=[];
for(i=0; i<wholeTable.length; i++)
{
    tempObj[i] ={Country:"", GDP:""}
    tempObj[i].Country=wholeTable[i].cells[1].innerText;
    tempObj[i].GDP=parseFloat(wholeTable[i].cells[2].innerText.replace(/[^\d\.\-]/g, ""));    
}
 */
const overallWR = document.querySelector("#SummonerLayoutContent> div.tabItem.Content.SummonerLayoutContent.summonerLayout-summary > div.SideContent > div.TierBox.Box > div > div.TierRankInfo > div.TierInfo > span.WinLose > span.winratio");
console.log(overallWR);
Program can be modified by command line to run a different keyword for the weekly notification:

java -jar StartAgendaBot.jar [keyword]     for runnable jars..

but by default the keyword is “Ojibway”

Two task bots scheduled: a weekly notification bot and a constant reply bot

Both make use of the same “scraping method”:
	Use PDFBox to parse each page and search for keyword
	Return a list of “context” strings for the keyword with the page they were located on

Weekly bot:
Using Jsoup, City Council Agenda page is parsed for most recent pdf which is stored (along with the URL) for use by itself and reply bot
After scraping, the weekly bot with go through up to 5 contexts and make tweets about them, with a half hour between tweet.
If there are no matches, it doesn't tweet anything.
It will then sleep for a week and repeat the process
	
Reply bot:
Every 10 seconds, a search will be made for tweets that start with @WindsorAlert
Respond to each, and add them to a cache. If they are in the cache, then dont respond.
When responding, it scrapes the same way as the weekly bot, but then picks a random context to respond to the user with.
If there is no keyword match, it sends a reply notifying them.

Made with Java and various libraries

Will need to setup your twitter oauth in a "twitter4j.properties" file in same folder as executable. It should look like this:

"oauth.consumerKey=******
oauth.consumerSecret=******
oauth.accessToken=******
oauth.accessTokenSecret=******"

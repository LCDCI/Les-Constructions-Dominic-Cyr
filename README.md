**Setup**


git clone https://github.com/LCDCI/Les-Constructions-Dominic-Cyr.git


cd Les-Constructions-Dominic-Cyr 


git remote add upstream https://github.com/LCDCI/Les-Constructions-Dominic-Cyr.git

**Note: make sure to be logged in to git on your console using**


git config --global user.email "you@example.com"


and


git config --global user.name "Your Name"



**Usage**


***Before starting every ticket***


git reset --hard origin/main


***then***


git switch -c YOUR-BRANCH-NAME


***Commiting***


** Make sure you are on your personal branch and not the main branch ** 


git add .


git commit -m "message of commit"


git push


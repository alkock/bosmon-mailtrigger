<snippet>
  <content><![CDATA[
# ${1:Project Name}

Das Programm ${1:Project Name} stellt eine Brücke von einem IMAP-Client zu <a href=http://www.bosmon.de>BosMon</a> dar.


## Allgemeines

### Warum gibt es dieses Projekt?
BosMon ist ein Programm zur Auswertung, Visualisierung und Weiterleitung von (u.A.) POCSAG Telegrammen.
Dabei können diese POCSAG Telegramme über verschiedene Kanäle (Funkscanner, DME oder Netzwerk) empfangen werden.
Es gibt allerdings Situationen wo die 3 möglichen Kanäle nicht ausreichen. So kann es zum Beispiel sein, dass die
Variante mittels DME zu teuer ist, Funkscanner aufgrund eingesetzter Verschlüsselung nicht zielführend sind oder
die Übermittlung via Netzwerk nicht erlaubt ist. Letzteres ist der Grund warum ich diese Email-Bridge implementiere.

Kurze Story: In unserer Feuerwehr haben wir 5 Standorte. In drei Standorten ist Internet vorhanden.
In Standort 1 und 2 ist jeweils eine BosMon-Instanz installiert die ihre Eingangssignale (POCSAG Telegramme) über
einen Oelmann LX2 DME empfangen (Redundante Anbindung). Diese Alarme werden dann auch auf's Smartphone gepusht, dass aber nur am Rande.
In Standort 3 sollte nun auch ein Alarmmonitor installiert werden. Allerdings wollte ich dort nicht auch nochmal 350 € für einen
DME samt Programmierstation in die Hand nehmen. Daher wollte ich die Alarme einfach über das Internet an die 3. Instanz weiterleiten.
Dies ist technisch kein Problem, allerdings ist dafür - wie ihr sicherlich wisst - das öffnen eines Ports notwendig.
Dies ist sicherheitstechnisch nicht so prall und hätte unsere Stadtverwaltung wahrscheinlich (wirklich gefragt habe ich nicht) nicht
genehmigt. Deshalb dachte ich mir - wandle ich den Inbound zu Outbound Traffic (Email statt Netzwerkereignis von außen).
 
Dies ist der Grund warum es dieses Projekt überhaupt gibt.

### Wäre es nicht besser ein BosMon Plugin zu schreiben anstatt ein extra Tool zu verwenden?
Ja, ohne Frage wäre dies besser, aber.... 
Ich komme aus der Linux und Java Welt - und ich hasse Windows. Ich bin zwar grundsätzlich im Besitz
des Quellcodes von BosMon und könnte sicherlich mit einiger Einarbeitungszeit auch ein Plugin
schreiben, aber lasst es mich kurz machen - ich will nicht. 
Daher habe ich mich, weil meine Zeit auch drückte, einfach für ein kleines Zusatztool entschieden.
Sollte jemand von euch diesen Code zu einem Plugin adaptieren wollen, so tut euch keinen Zwang an :)

## Installation
TBD:

## Konfiguration
TBD:

##Arbeitsweise
TBD:

##Offene Bugs/TODOs
<ul>
<li>IMAP Client korrekt verwenden. Aktuell wird noch alle X Sekunden eine Verbindung aufgebaut und aktiv geguckt ob neue Alarme vorliegen. Also quasi polling.</li>
<li>RIC und Meldungstext aus Email konfigurierbar auslesen. Aktuell muss die Email noch ein konkretes Format haben.</li>
</ul>
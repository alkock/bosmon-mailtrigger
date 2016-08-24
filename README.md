# bosmon-mailtrigger

Das Programm bosmon-mailtrigger (Arbeitsname) stellt eine Brücke von einem IMAP-Client zu <a href=http://www.bosmon.de>BosMon</a> dar.


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

## Was wird benötigt?
Um Alarme via Mail in Bosmon empfangen zu können braucht ihr folgendes:
<ul>
<li>Java JRE in Version 8</li>
<li>BosMon (wer hätte das gedacht)</li>
<li>Ein Email Konto das ausschließlich für das Empfangen von AlarmEmails vorhanden ist</li>
<ul><li>Achtung: jede eingehende Email an die auszuwertende Adresse wird ohne Rückfrage sofort nach der Verarbeitung gelöscht</li></ul>
<ul><li>Ferner sollte dieses Email Konto auch nirgends ausgelesen werden! Also nur durch dieses Programm bearbeitet werden</li></ul>
<li>Eine automatische Alarmierung via Mail (entweder von der Leitstelle oder von einer anderen BosMon Instanz (zu empfehlen)</li>
<li>Grundlegende Kenntnisse mit der Windows Kommandozeile</li>
</ul>

## Installation
Aktuell gibt es noch keinen Installer oder ähnliches. Wird's wahrscheinlich auch nie geben.
Ihr müsst euch einfach unter dem Punkt <a href=https://github.com/hannehomuth/bosmon-mailtrigger/releases>Releases</a> die letzte Version runterladen und bei euch irgendwo entpacken.
Danach editiert ihr die Konfigurationsdatei nach euren Bedingungen (siehe Punkt Konfiguration) und startet das Programm.

Dazu geht ihr in Windows auf die Kommandozeile (cmd) und tippt folgenden Befehl ein.

```java -jar Pfad\zur\jar\datei\bosmon-mailreader-<version>-jar-with-dependencies.jar \Pfad\zur\Konfigurationsdatei\bosmailreader.properties```


Es empfiehlt sich natürlich das Programm als Dienst/Service starten zu lassen. Wer sich damit auskennt kann das mal probieren, ich habe das
allerdings noch nicht getestet.

## Konfiguration
Dieses Programm ist ein Kommandozeilentool welches keine Oberfläche besitzt.
Somit kann auch die Konfiguration auch nicht über eine Oberfläche durchgeführt werden.

Ferner ist dafür eine Konfigurationsdatei zu verwenden. Die Konfigurationsdatei hat 
das Java Properties Format (KEY=VALUE). Alle Konfigurationsparamter werden in folgender
Tabelle erläutert.

| KEY        | Defaultwert| Verpflichtend|  Beschreibung  |
| :--------- |:----------:|:------------:|:---------------  |
| IMAP_SERVER| -|ja|Die Hostadresse des IMAP Servers der zum Abrufen der Emails angesprochen werden soll.  Beispiel (imap.1blu.de)
|IMAP_USER	|-|ja|Der Nutzername der zum Abrufen der Emails verwendet werden soll. (Beispiel: alarmmailUser)
|IMAP_PASS|-|ja|Das Passwort welches zum Abrufen der Emails verwendet werden soll. (Beispiel ****** :-) )
|ALLOWED_SENDER| - | nein | Die erlaubte Absender-Adresse von der Emails angenommen werden. Dieses Feld ist nicht zwingend anzugeben, aber ich rate dazun. Jeder, auch wenn man sich noch so gut schützt, hat schon mal SPAM bekommen. Und ich nehme an ihr wollt nicht den SPAM an BosMon und eventuell sogar per Push an eure Kameraden weiterleiten. Wenn ihr also eure Alarme immer von einer festen Email-Adresse geliefert bekommt, tragt diese lieber hier ein.
|SENDER_ADDRESS_VALIDATION|false|nein|Soll die Absender-Validierung eingeschaltet werden oder nicht. Ich empfehle ja!
|BOSMON_USER|-|nein|Benutzer der zur Authentifizierung am BosMon Webserver genommen werden soll. Diesen könnt ihr unter Bearbeiten ->  Benutzerverwaltung anlegen/nachschauen. Ich persönlich verwende das nicht, weil der Server von BosMon nur lokal im Netzwerk erreichbar ist. Ist noch nicht getestet, sollte aber gehen.
|BOSMON_PASS|-|nein|Passwort für den Benutzer (siehe KEY BOSMON_USER)
|BOSMON_CHANNEL_NAME|in|nein|Name des Webkanals an den die Meldungen weitergeleitet werden sollen.
|BOSMON_SERVER_NAME|127.0.0.1|nein|Adresse des BosMon-Servers. Sollte wahrscheinlich "localhost"/"127.0.0.1" sein.
|BOSMON_SERVER_PORT|80|nein|Die Portnummer des BosMon-Servers. Kann in den Einstellungen des WebServers in BosMon nachgelesen werden.
|MAX_ALARM_AGE|5|nein| Das maximale Alter (Absende-Datum) der Email in Minuten. Dies hat folgenden Grund. Man nehme an ihr zeigt die Alarme in BosMon nicht nur an, sondern leitet diese auch noch per AlarmApp oder sonstigen Push Notifications weiter. Wenn ihr euch vorstellt ihr hattet einen Stromausfall oder habt euren PC aus sonst irgendwelchen Gründen mal ausgeschaltet und schaltet ihn, z.B. nach 1 Woche, wieder ein, kommen alle Emails der letzten Woche rein. Wenn die nun alle an BosMon weitergeleitet würden und via Push an eure Kameraden rausgehen wirds für euch sicherlich teuer, so viel Bier könnt ihr gar nicht kaufen. Daher solltet ihr euch Gedanken machen wie alt den eine Email sein darf um noch verarbeitet zu werden. Ich habe für mich 5 Min festgelegt.
|ALARM_SUPRESS_TIME|5|nein|Unterdrückung von Mehrfachalarmierungen mit gleichem Text in Minuten (experimentell)
|POLLING_SECONDS|30|nein|Anzahl in Sekunden zwischen dem Abrufen des Email-Postfachs. (Auch wenn wir hier IMAP verwenden, so richtig IMAP Push habe ich noch nicht implementiert, kommt aber noch)
|DEBUG_MAIL|false|nein|Log-Level für das Abholen der Emails. (Für Testzwecke)
|FORCE_RIC|-|nein|Forciere bei der Alarmierung immer die hier eingegebene RIC Adresse. Das ist aktuell eher ein Workaround für Email-Formate die nicht dem entsprechen was ich mir so erwartet habe.
|LINE_SEPARATOR|\||nein|Wenn der Email-Text aus mehreren Zeilen besteht wurde bei mir immer nur die erste Zeile an BosMon übertragen. Daher wandle ich den Text vorher um und entferne alle Zeilenumbrüche durch das angegebene Zeichen

##Arbeitsweise
TBD:
Email auslesen, via Http weiterleiten (alles im X Sekunden Interval)

##Support
Falls ihr Hilfe benötigt, Anregungen oder Fragen habt, so könnt ihr mich gerne unter it-support(at)ffw-beetz-sommerfeld.de kontaktieren.

##Offene Bugs/TODOs
<ul>
<li>IMAP Client korrekt verwenden. Aktuell wird noch alle X Sekunden eine Verbindung aufgebaut und aktiv geguckt ob neue Alarme vorliegen. Also quasi polling.</li>
<li>RIC und Meldungstext aus Email konfigurierbar auslesen. Aktuell muss die Email noch ein konkretes Format haben.</li>
<li>mehrere erlaubte Absender-Adressen erlauben</li>
<li>Multipart Mails unterstützen (Bereits bedingt implementiert. Eventuell Anpassungen notwendig)</li>
</ul>

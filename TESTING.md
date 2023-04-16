# Testovanie a vyhodnotenie projektu

## Lokálne testovanie

Pre lokálne testovanie odovzdaného študentského projektu stačí skopírovať obsah priečinku [`./src/test`](./src/test)
tohto projektu
do rovnako nazvaného priečinku v študentskom projekte (cieľového testovaného projektu).

Pre spustením testov je potrebné sa presvedčiť, že PostgreSQL (resp. uprednostňovaná) je zapnutá a je vytvorená databáza
vsa_pr1, ktorá je prázdna.

Pre spustenie testov stačí spustiť maven goal `clean test`, resp. spustiť príkaz:

```shell
mvn clean test
```

Priebeh testov je možné sledovať v konzolovom výpise kde bol príkaz spustený (buď v IDE, alebo v terminály).

Po spustení testov sú vygenerované surefire reporty, ktoré vypovedajú o výsledku jednotlivých testov. Reporty sú na
v priečinku [`./target/surefire-reports`](./target/surefire-reports) v testovanom projekte.

# Travel
Non viaggiare con la mente, viaggia con noi✈️

# Istruzioni utilizzo telefono fisico backend+frontend
Creare nella root di Travel il file .env con il seguente contenuto: HOST_IP=tuo-ip-locale-del-pc
Nel file local.properties della cartella frontend aggiungere una riga con: backend.url=http://<IP_DEL_PROPRIO_PC>:8080/

## Allineamento Libreria Condivisa (in_common)
Il progetto utilizza un modulo Java condiviso (`in_common`) per i DTO. Poiché il file binario `.jar` compilato è escluso dal controllo versione (`.gitignore`), al primo clone (o ogni volta che vengono modificati i DTO in `in_common`), è necessario compilarlo e copiarlo nella cartella delle librerie del frontend.

### Procedura automatica (Windows - PowerShell):
Esegui questo comando dalla cartella principale (root) del progetto:
```powershell
mvn -f in_common/pom.xml clean package ; Copy-Item -Path "in_common\target\common-dtos-1.0.0.jar" -Destination "frontend\app\libs\common-dtos-1.0.0.jar" -Force
```

### Procedura automatica (macOS / Linux / Bash):
Esegui questo comando dalla cartella principale (root) del progetto:
```bash
mvn -f in_common/pom.xml clean package && cp in_common/target/common-dtos-1.0.0.jar frontend/app/libs/
```

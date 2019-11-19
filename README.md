## Mint the DOI for Publon peer review

# Requirement

JDK 1.7 and apache.poi jar 

```java
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
```
10.5524-review.101926.xml is the example review metadata to mint DOI.

sep2-2019.xls is the example spreadsheet for all reviews in Sep 2019.

doi is the file to record start doi number.

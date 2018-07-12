package licenta.medicaldata.crypto;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Base64;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    AesEncryption aesEncryption = new AesEncryption();

    @Override
    public String convertToDatabaseColumn(String ccNumber) {
      try {

         return Base64.getEncoder().encodeToString(aesEncryption.encryptCall(ccNumber.getBytes()));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
      try {
          return new String(aesEncryption.decryptCall(Base64.getDecoder().decode(dbData)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
}
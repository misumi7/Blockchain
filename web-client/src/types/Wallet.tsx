export class Wallet {
      publicKey : string;
      encryptedPrivateKey : string;
      iv : string;
      salt : string;
      name : string;

      constructor(publicKey: string, encryptedPrivateKey: string, iv: string, salt: string, name: string) {
            this.publicKey = publicKey;
            this.encryptedPrivateKey = encryptedPrivateKey;
            this.iv = iv;
            this.salt = salt;
            this.name = name;
      }
}
import {Wallet} from '../types/Wallet';
import { ec as EC } from "elliptic";
import * as argon2Module from 'argon2-wasm';
import { sha256 } from 'hash-wasm';

const argon2 = await (argon2Module).default(); // ???
const ec = new EC("secp256k1");

export const handleWalletCreation = () => {
      console.log("Create New Wallet button clicked");
      const ec = new EC("secp256k1");

      const keyPair = ec.genKeyPair();

      const privateKey = keyPair.getPrivate("hex");

      const publicKey = keyPair.getPublic("hex");

      console.log("Private Key:", privateKey);
      console.log("Public Key:", publicKey);
};

async function deriveKeyFromPin(pin: string, salt: string): Promise<Uint8Array> {
      const result = await argon2.hash({
            pass: pin,
            salt: new TextEncoder().encode(salt),
            type: argon2.ArgonType.Argon2id,
            time: 2,
            mem: 65536,
            parallelism: 1,
            hashLen: 32
      });

      return result.hash;
}

async function getAesKey(keyBytes : Uint8Array) : Promise<CryptoKey> {
      return crypto.subtle.importKey(
            "raw",
            keyBytes,
            {
                  name: "AES-GCM"
            },
            false,
            ["decrypt"]
      );
}

export async function getTransactionHash(sender : string, receiver : string, timeStamp : number, fee : number, amount : number): Promise<Buffer> {
      const hashHex = await sha256(sender + receiver + amount + fee + timeStamp);
      return Buffer.from(hashHex, 'hex');
}

function signHashWithPrivateKey(
            hash: Buffer,
            privateKeyHex: string
      ): { r: string; s: string; signatureHex: string } {
            const key = ec.keyFromPrivate(privateKeyHex, "hex");
            const signature = key.sign(hash);

            const r = signature.r.toString("hex");
            const s = signature.s.toString("hex");

            const derSign = signature.toDER("hex");

            return {r, s, signatureHex: derSign};
}

async function decryptAESGCM(
            key: CryptoKey,
            iv: Uint8Array,
            ciphertext: Uint8Array,
            //authTagLength: number = 128
      ): Promise<string> {
            const decrypted = await crypto.subtle.decrypt(
            {
                  name: "AES-GCM",
                  iv,
                  //tagLength: authTagLength // в битах (128 по умолчанию)
            }, key, ciphertext);

      return new TextDecoder().decode(decrypted);
}

export const signTransactionHash = async (hash : Buffer, ecryptedPrivateKey : string, pin : string, salt : string, iv : string) : Promise<string> => {
      const keyBytes = await deriveKeyFromPin(pin, salt);
      const aesKey = await getAesKey(keyBytes);
      const decryptedPrivateKey = await decryptAESGCM(aesKey, new TextEncoder().encode(iv), new TextEncoder().encode(ecryptedPrivateKey));

      return signHashWithPrivateKey(hash, decryptedPrivateKey).signatureHex;
}
declare module 'argon2-wasm' {
      export enum ArgonType {
            Argon2d = 0,
            Argon2i = 1,
            Argon2id = 2
      }

      export interface Argon2Params {
            pass: string | Uint8Array;
            salt: Uint8Array;
            time?: number;
            mem?: number;
            hashLen?: number;
            parallelism?: number;
            type?: ArgonType;
      }

      export interface Argon2HashResult {
            hash: Uint8Array;
            encoded: string;
      }

      export interface Argon2 {
            hash(params: Argon2Params): Promise<Argon2HashResult>;
            ArgonType: typeof ArgonType;
      }

      export function Argon2Browser(): Promise<Argon2>;
}
import axios from "axios";

export const getWalletBalance = async (walletPublicKey : string) : Promise<string> => {
      try {
            const response = await axios.get<string>(`/api/utxo/balance`, {
                  params: {walletPublicKey}
            });
            return response.data;
      }
      catch (error) {
            console.error("Error fetching wallet balance:", error);
            return "";
      }
}
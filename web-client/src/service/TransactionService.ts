import axios from "axios";

export const getInputsRequired = async (walletPublicKey: string, amount: number): Promise<number> => {
      try {
            const response = await axios.get<number>('/api/utxo/inputs-required', {
                  params: { 
                        walletPublicKey: walletPublicKey,
                        amount: amount
                  }
            });
            return response.data;
      }
      catch (error) {
            console.error("Error fetching inputs required:", error);
            return 0;
      }
}

export const getRecommendedFee = async (): Promise<number> => {
      try {
            const response = await axios.get<number>('/api/transactions/fee');
            return response.data;
      }
      catch (error) {
            console.error("Error fetching recommended fee:", error);
            return 0;
      }
}
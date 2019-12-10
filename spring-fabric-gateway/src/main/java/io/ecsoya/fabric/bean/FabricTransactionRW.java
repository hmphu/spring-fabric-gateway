package io.ecsoya.fabric.bean;

import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset.KVRead;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset.KVWrite;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset.Version;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.ecsoya.fabric.json.JsonUtils;
import lombok.Data;

@Data
public class FabricTransactionRW {

	private int index;

	private String type = "";

	private String key = "";

	private String value = "";

	private String remarks = "";

	public static FabricTransactionRW fromRead(int index, KVRead read) {
		if (read == null) {
			return null;
		}
		FabricTransactionRW txRead = new FabricTransactionRW();
		txRead.setIndex(index);
		String key = read.getKey();
		String[] split = key.split(DELIMITER, 0);
		if (split.length != 0) {
			txRead.setKey(split[split.length - 1]);
			txRead.setType(split[split.length - 2]);
		}
		Version version = read.getVersion();
		if (version != null) {
			txRead.setValue("blockNum: " + version.getBlockNum() + ", txNum: " + version.getTxNum());
		}
		return txRead;
	}

	public static FabricTransactionRW fromWrite(int index, KVWrite write) {
		if (write == null) {
			return null;
		}
		FabricTransactionRW txWrite = new FabricTransactionRW();
		txWrite.setIndex(index);
		String compositeKey = write.getKey();
		String[] split = compositeKey.split(DELIMITER, 0);
		if (split.length != 0) {
			txWrite.setKey(split[split.length - 1]);
			txWrite.setType(split[split.length - 2]);
		}
		txWrite.setValue(simplifier(write.getValue().toStringUtf8()));
		txWrite.setRemarks(Boolean.toString(write.getIsDelete()));
		return txWrite;
	}

	private static String simplifier(String value) {
		try {
			JsonElement tree = new JsonParser().parse(value);
			if (tree.isJsonObject()) {
				JsonObject obj = tree.getAsJsonObject();
				JsonElement valuesObj = obj.get("values");
				if (valuesObj != null) {
					return JsonUtils.toJson(valuesObj);
				}
			}
		} catch (Exception e) {
			return value;
		}
		return value;

	}

	private static final String DELIMITER = new String(Character.toChars(Character.MIN_CODE_POINT));
}
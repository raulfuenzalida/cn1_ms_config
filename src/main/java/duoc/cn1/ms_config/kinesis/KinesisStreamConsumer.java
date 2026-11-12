package duoc.cn1.ms_config.kinesis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.Shard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Polling genérico de un Kinesis Data Stream.
 * Mantiene un iterador por shard en memoria: al reiniciar el servicio
 * vuelve a TRIM_HORIZON y la idempotencia de processed_events
 * evita reprocesar eventos ya aplicados.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KinesisStreamConsumer {

	private static final int MAX_RECORDS_PER_POLL = 100;

	private final KinesisClient kinesisClient;

	// streamName -> (shardId -> shardIterator)
	private final Map<String, Map<String, String>> shardIterators =
		new ConcurrentHashMap<>();

	public void poll(String streamName, Consumer<Record> handler) {
		Map<String, String> iterators =
			shardIterators.computeIfAbsent(streamName, k -> new ConcurrentHashMap<>());

		List<Shard> shards = kinesisClient.listShards(
			ListShardsRequest.builder().streamName(streamName).build()
		).shards();

		for (Shard shard : shards) {
			String shardId = shard.shardId();

			String iterator = iterators.computeIfAbsent(shardId, id ->
				getShardIterator(streamName, id));

			if (iterator == null) {
				continue;
			}

			GetRecordsResponse response;
			try {
				response = kinesisClient.getRecords(
					GetRecordsRequest.builder()
						.shardIterator(iterator)
						.limit(MAX_RECORDS_PER_POLL)
						.build()
				);
			} catch (Exception e) {
				// Iterator expirado/inválido: se descarta para que el
				// próximo poll cree uno nuevo desde TRIM_HORIZON.
				// La idempotencia de processed_events evita reprocesar.
				log.error("Error en getRecords {}/{}: {}. Se renueva iterador.",
					streamName, shardId, e.getMessage());
				iterators.remove(shardId);
				continue;
			}

			List<Record> records = response.records();

			if (records.isEmpty()) {
				continue;
			}

			for (Record record : records) {
				handler.accept(record);
			}

			String lastSequence = records.get(records.size() - 1).sequenceNumber();
			iterators.put(shardId, getShardIteratorAfter(streamName, shardId, lastSequence));
		}
	}

	private String getShardIterator(String streamName, String shardId) {
		try {
			return kinesisClient.getShardIterator(
				GetShardIteratorRequest.builder()
					.streamName(streamName)
					.shardId(shardId)
					.shardIteratorType(
						software.amazon.awssdk.services.kinesis.model.ShardIteratorType.TRIM_HORIZON)
					.build()
			).shardIterator();
		} catch (Exception e) {
			log.error("Error obteniendo shard iterator para {}/{}: {}",
				streamName, shardId, e.getMessage());
			return null;
		}
	}

	private String getShardIteratorAfter(String streamName, String shardId, String sequenceNumber) {
		try {
			return kinesisClient.getShardIterator(
				GetShardIteratorRequest.builder()
					.streamName(streamName)
					.shardId(shardId)
					.shardIteratorType(
						software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AFTER_SEQUENCE_NUMBER)
					.startingSequenceNumber(sequenceNumber)
					.build()
			).shardIterator();
		} catch (Exception e) {
			log.error("Error obteniendo iterator post-secuencia {}/{}: {}",
				streamName, shardId, e.getMessage());
			return null;
		}
	}
}

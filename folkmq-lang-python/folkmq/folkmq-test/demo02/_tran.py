import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
                    .nameAs("demoapp")
                    .connect())

    client.transactionCheckback(lambda m:
        log.info("check: " + m.getBodyAsString()) and m.acknowledge(True)
    )

    # 订阅
    await client.subscribe("test", None, True, lambda message:
        log.info(message.getBodyAsString())
    )

    # 发布
    tran = client.newTransaction()
    try:
        await client.publish("test", MqMessage("hot-1").transaction(tran))
        await client.publish("test", MqMessage("hot-2").transaction(tran))
        await client.publish("test", MqMessage("hot-3").transaction(tran))
        await tran.commit()
    except Exception as e:
        await tran.rollback()
        log.error(e)

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())


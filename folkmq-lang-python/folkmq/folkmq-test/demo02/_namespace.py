import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
                    .nameAs("demoapp")
                    .namespaceAs("DEFAULT")
                    .connect())

    # 订阅
    await client.subscribe("test.topic", None, True, lambda message:
        log.info(message.getBodyAsString())
    )

    # 发布
    for i in range(10):
        await client.publish("test.topic", MqMessage("hot-" + str(i)))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())


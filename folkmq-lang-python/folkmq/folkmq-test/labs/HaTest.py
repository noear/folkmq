import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.createClient("folkmq://127.0.0.1:18601?ak=ak1&sk=sk1",
                        "folkmq://127.0.0.1:18602?ak=ak1&sk=sk1")
                    .nameAs("demoapp")
                    .connect())

    # 订阅
    await client.subscribe("demo", None, None, lambda message:
        log.info(message.getBodyAsString())
    )

    i:int = 0
    # 发布
    while True:
        await client.publish("demo", MqMessage("test-" + str(i)))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())


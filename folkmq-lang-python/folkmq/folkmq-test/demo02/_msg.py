import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.create_client("folkmq:ws://127.0.0.1:18602")
                    .name_as("demoapp")
                    .connect())

    # 订阅
    await client.subscribe("test", None, True, lambda message:
        log.info(message.get_body_as_string())
    )

    # 发布
    for i in range(10):
        await client.publish("test", MqMessage("hot-" + str(i)))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())


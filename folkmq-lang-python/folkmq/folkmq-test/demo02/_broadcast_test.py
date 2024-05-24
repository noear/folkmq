import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ


async def main():
    client1 = await (FolkMQ.create_client("folkmq:ws://127.0.0.1:18602")
                    .name_as("demoapp")
                    .connect())
    client2 = await (FolkMQ.create_client("folkmq:ws://127.0.0.1:18602")
                     .name_as("demoapp")
                     .connect())

    # 订阅
    await client1.subscribe("test", None, True, lambda message:
        log.info(message.get_body_as_string())
    )
    await client2.subscribe("test", None, True, lambda message:
        log.info(message.get_body_as_string())
    )

    # 发布
    await client1.publish("test", FolkMQ.new_message("hot-1"))
    await client1.publish("test", FolkMQ.new_message("hot-2").broadcast(True))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())

